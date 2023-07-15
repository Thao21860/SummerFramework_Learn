package org.example.ioc;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.annotation.*;
import org.example.config.PropertyResolver;
import org.example.exception.*;
import org.example.scan.ResourceResolver;
import org.example.test.ConfigT1;
import org.example.utils.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

// 负责bean管理
public class AnnotationConfigApplicationContext {
    PropertyResolver propertyResolver;
    Map<String,BeanDefinition> beans;
    // 创建实例集合
    Set<String> creatingBeanNames;
    // 注册beans
    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws URISyntaxException, IOException, ClassNotFoundException {
        // 扫描所有Bean 的 class类型
        Set<String> beanClassNames = scanForClassNames(configClass);
        // 创建beans definition
        this.beans = createBeanDefinitions(beanClassNames);

        this.propertyResolver = propertyResolver;

        this.creatingBeanNames = new HashSet<>();
        // 首先创建configuration类中的bean
        this.beans.values().stream()
                        .filter(this::isConfiguration).map(def->{
                        //创建实例
                        createBeanAsEarlySingleton(def);
                        return def.getName();
                        }).collect(Collectors.toList());
        // 过滤剩余
        List<BeanDefinition> defs = this.beans.values().stream()
                        .filter(def->def.getInstance() == null).sorted().collect(Collectors.toList());
        // 创建普通bean
        defs.forEach(def ->{
            if(def.getInstance() == null){
                createBeanAsEarlySingleton(def);
            }
        });
        // 初始化所有bean
//        this.beans.values().stream().sorted().forEach(def -> {
//
//        });
        System.out.println("application Construct done");
    }

    private Map<String, BeanDefinition> createBeanDefinitions(Set<String> classNameSet) throws ClassNotFoundException {
        Map<String,BeanDefinition> defs = new HashMap<>();
        for(String name: classNameSet){
            Class<?> clazz = null;
            try{
                clazz = Class.forName(name);
            } catch (ClassNotFoundException e){
                throw new BeanCreationException(e);
            }
            // 查找Component标注
            // 所有操作被ClassUtils封装
            // TODO import的不会含有component注解，会丢失
            Component component = ClassUtils.findAnnotation(clazz, Component.class);
            if(component != null){
                //排除抽象、私有类
                int mod = clazz.getModifiers();
                if(Modifier.isAbstract(mod)){
                    throw new BeanDefinitionException(clazz.getName() + "is Abstract class");
                }
                if(Modifier.isPrivate(mod)){
                    throw new BeanDefinitionException(clazz.getName() + "is private class");
                }
                // 创建bena
                String beanName = ClassUtils.getBeanName(clazz);

                BeanDefinition beanDefinition = new BeanDefinition(beanName,clazz,getSuitableConstructor(clazz),
                        getOrder(clazz),clazz.isAnnotationPresent(Primary.class),
                        // init destroy
                        null,null,
                        //postConstruct
                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                        // preDestroy
                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinition(defs,beanDefinition);
                // 顺便找到configure bean, configure内使用了Component
                Configuration configuration = ClassUtils.findAnnotation(clazz, Configuration.class);
                if(configuration != null){
                    // 扫描@Bean 并加入
                    scanFactoryMethods(beanName, clazz, defs);
                }
            }
            // 扫描import并加入
            Import imp = ClassUtils.findAnnotation(clazz, Import.class);
            if(imp != null){
                scanImport(classNameSet,clazz,defs);
            }
        }
        return defs;
    }

    protected Set<String> scanForClassNames(Class<?> configClass) throws URISyntaxException, IOException {
        // 获取@ComponetScan注解内包路径
        ComponentScan scan = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        // 默认扫描包路径为configClass所在包
        String[] scanPackage = scan == null || scan.value().length == 0? new String[] {configClass.getPackage().getName()} : scan.value();
        Set<String> classNameSet = new HashSet<>();
        // 开始扫描路径下所有class文件
        for(String pkg: scanPackage){
            ResourceResolver rr =  new ResourceResolver(pkg);
            List<String> classList = rr.scan(res ->{
                // class文件
                String name = res.getName();
                if(name.endsWith(".class")){
                    return name.substring(0,name.length() - 6).replace("/",".").replace("\\",".");
                }
                return null;
            });
            classNameSet.addAll(classList);
        }
        // 搜索@import
        Import importConfig = configClass.getAnnotation(Import.class);
        if(importConfig != null){
            for(Class<?> importConfigClass: importConfig.value()){
                String importClassName = importConfigClass.getName();
                // 可能已经存在,忽略
                classNameSet.add(importClassName);
            }
        }
        return classNameSet;
    }

    //by-name查找
    @Nullable
    public BeanDefinition findBeanDefinition(String name){
        return this.beans.get(name);
    }
    // by-type
    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> type){
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if(defs.isEmpty()) return null;
        if (defs.size() == 1) return defs.get(0);

        //查找标注为primary的bena
        List<BeanDefinition> primaryDefs = defs.stream().filter(BeanDefinition::isPrimary).collect(Collectors.toList());
        if(primaryDefs.size() == 1) return defs.get(0);
        if (primaryDefs.isEmpty()){
            // 不存在@primary标注，抛出错误
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        }else {
            throw new NoUniqueBeanDefinitionException(String.format("Multiple bean with type '%s' found, but multiple @Primary specified.", type.getName()));
        }
    }
    List<BeanDefinition> findBeanDefinitions(Class<?> type){
        return this.beans.values().stream()
                // 按类型筛选，isAssignableFrom 判断是否相同，或是否为type父类
                .filter(def -> type.isAssignableFrom(def.getBeanClass()))
                //排序
                .sorted().collect(Collectors.toList());
    }

    Constructor<?> getSuitableConstructor(Class<?> clazz){
        Constructor<?>[] cons = clazz.getConstructors();
        if(cons.length == 0){
            // 使用默认无参构造
            cons = clazz.getDeclaredConstructors();
            if(cons.length != 1){
                throw new BeanDefinitionException("more than one Constructor in class" + clazz.getName());
            }
        }
        if(cons.length != 1){
            throw new BeanDefinitionException("more than one Constructor in class" + clazz.getName());
        }
        return cons[0];
    }

    int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }
    void addBeanDefinition(Map<String,BeanDefinition> defs, BeanDefinition def){
        if(defs.put(def.getName(),def) != null){
            System.out.println(def);
            throw new BeanDefinitionException("Duplicate bean name:" + def.getName());
        }
    }

    // 用于configure 内 @Bean扫描
    void scanFactoryMethods(String factoryBeanName, Class<?> clazz, Map<String,BeanDefinition> defs){
        for(Method method: clazz.getDeclaredMethods()){
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null){
                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be private.");
                }
                // 获取返回bean类型
                Class<?> beanClass = method.getReturnType();
                if(beanClass.isPrimitive()){
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return primitive type.");
                }
                if(beanClass == void.class || beanClass == Void.class){
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return void.");
                }
                // 创建
                BeanDefinition def = new BeanDefinition(ClassUtils.getBeanName(method),beanClass,factoryBeanName,
                        method,getOrder(method),method.isAnnotationPresent(Primary.class),
                        bean.initMethod().isEmpty()? null:bean.initMethod(),
                        bean.destroyMethod().isEmpty()? null:bean.destroyMethod(),
                        null,null);
                addBeanDefinition(defs,def);
            }
        }
    }

    void scanImport(Set<String> classNameSet, Class<?> clazz, Map<String,BeanDefinition> defs) throws ClassNotFoundException {
        // 读取import参数
        Import imp = ClassUtils.findAnnotation(clazz, Import.class);
        Class<?>[] impList = imp.value();
        for(Class<?> impE: impList){
            if(classNameSet.contains(impE.getName())){
                //创建对应的BeanDefinition对象
                String name = impE.getSimpleName();
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                String re = impE.getName();
                try{
                    Class<?> reClazz = Class.forName(re);
                    BeanDefinition def = new BeanDefinition(name,reClazz,getSuitableConstructor(reClazz),
                            getOrder(reClazz),reClazz.isAnnotationPresent(Primary.class),
                            null,null,
                            ClassUtils.findAnnotationMethod(reClazz, PostConstruct.class),
                            ClassUtils.findAnnotationMethod(reClazz, PreDestroy.class));
                    addBeanDefinition(defs,def);
                }catch (Exception e){
                    throw new ClassNotFoundException(String.format("can not found class:%s",re));
                }
//                System.out.println(name);
            }
        }
    }

    // 过滤configuration 使用
    boolean isConfiguration(BeanDefinition def){
        return ClassUtils.findAnnotation(def.getBeanClass(), Configuration.class) != null;
    }

    // 查找bean
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name){
        BeanDefinition def = this.beans.get(name);
        if(def == null){
            throw new BeansException(String.format("no such bean defined with name '%s'",name));
        }
        return (T) def.getRequiredInstance();
    }

    //创建
    public Object createBeanAsEarlySingleton(BeanDefinition def){
        if(!this.creatingBeanNames.add(def.getName())){
            // 重复创建,检测到循环依赖
            throw new UnsatisfiedDependencyException();
        }

        // 判定为工厂方法还是构造方法创建
        Executable createFn = def.getFactoryName() == null? def.getConstructor():def.getFactoryMethod();
        // 获取参数上的注解
        final Parameter[] parameters = createFn.getParameters();
        Object[] args = new Object[parameters.length];
        // 一个参数可能会有多个注解
        final Annotation[][] parametersAnno = createFn.getParameterAnnotations();
        // 遍历参数
        for(int i = 0; i < parameters.length; i++){
            final Parameter param = parameters[i];
            final Annotation[] paramAnnos = parametersAnno[i];
            // 获取注解
            final Value value = ClassUtils.getAnnotation(paramAnnos, Value.class);
            final AutoWired autoWired = ClassUtils.getAnnotation(paramAnnos, AutoWired.class);

            // configuration中bean的工厂方法,不允许使用Autowired注入
            final boolean isConfiguration = isConfiguration(def);
            if(isConfiguration && autoWired != null){
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when create @Configuration bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
            }

            if(value != null && autoWired != null){
                throw new BeanCreationException(
                        String.format("Cannot specify both @Autowired and @Value when create bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
            }
            if(value == null && autoWired == null){
                throw new BeanCreationException(
                        String.format("must specify  @Autowired or @Value when create bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
            }
            // 参数类型
            final Class<?> type = param.getType();

            if(value != null){
                // 使用 @value 注入 值
                args[i] = this.propertyResolver.getRequiredProperty(value.value(),type);
            }else {
                // autowired 注入 引用类型
                String name = autoWired.name();
                boolean required = autoWired.value();
                BeanDefinition dependsOnDef = name.isEmpty()? findBeanDefinition(type):findBeanDefinition(name);

                if(dependsOnDef != null){
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    // 进入此片段，isConfiguration一定为false
                    if(autowiredBeanInstance == null && ! isConfiguration){
                        // 依赖类未创建，且不是configuration时，递归
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                }else {
                    args[i] = null;
                }
            }
        }

        // 创建bean实例
        // 强依赖注入
        Object instance = null;
        if(def.getFactoryName() == null){
            // constructor 创建
            try{
                instance = def.getConstructor().newInstance(args);
            }catch (Exception e){
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        }else {
            // 工厂方法创建
            Object configInstance = getBean(def.getFactoryName());
            try{
                instance = def.getFactoryMethod().invoke(configInstance,args);
            }catch (Exception e){
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        }
        def.setInstance(instance);
        return def.getInstance();
    }

}