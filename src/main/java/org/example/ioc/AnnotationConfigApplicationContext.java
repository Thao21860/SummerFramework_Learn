package org.example.ioc;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.annotation.*;
import org.example.config.PropertyResolver;
import org.example.exception.BeanCreationException;
import org.example.exception.BeanDefinitionException;
import org.example.exception.NoUniqueBeanDefinitionException;
import org.example.scan.ResourceResolver;
import org.example.utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

// 负责bean管理
public class AnnotationConfigApplicationContext {
    Map<String,BeanDefinition> beans;

    // 注册beans
    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws URISyntaxException, IOException {
        // 扫描所有Bean 的 class类型
        Set<String> beanClassNames = scanForClassNames(configClass);
        // 创建beans definition
        this.beans = createBeanDefinitions(beanClassNames);
        System.out.println("done");
    }

    private Map<String, BeanDefinition> createBeanDefinitions(Set<String> classNameSet) {
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
                if(!classNameSet.contains(importClassName)){
                    classNameSet.add(importClassName);
                }
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

    void scanImport(Set<String> classNameSet, Class<?> clazz, Map<String,BeanDefinition> defs){
        // 读取import参数
        Import imp = ClassUtils.findAnnotation(clazz, Import.class);
        Class<?>[] impList = imp.value();
        for(Class<?> impE: impList){
            if(classNameSet.contains(impE.getName())){
                //创建对应的BeanDefinition对象
                String name = impE.getSimpleName();
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                System.out.println(name);
                BeanDefinition def = new BeanDefinition(name,clazz,getSuitableConstructor(clazz),
                                                        getOrder(clazz),clazz.isAnnotationPresent(Primary.class),
                                                        null,null,
                                                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                                                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinition(defs,def);
            }
        }
    }


}
