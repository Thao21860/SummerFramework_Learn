package org.example.utils;

import jakarta.annotation.Nullable;
import org.example.annotation.Bean;
import org.example.annotation.Component;
import org.example.exception.BeanDefinitionException;
import org.example.exception.NoUniqueBeanDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// 处理注解
public class ClassUtils {
    /**
     * 递归查找bean
     * <A extends Annotation> A必须为Annotation接口的子类
     */
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annoClass){
        // 在target类中查找annoClass类的注解
        A a = target.getAnnotation(annoClass);
        // 遍历包含a的所有注解
        for (Annotation anno : target.getAnnotations()) {
            Class<? extends Annotation> annoType = anno.annotationType();
            //排除元注解
            if (!annoType.getPackage().getName().equals("java.lang.annotation")) {
                // 递归
                A found = findAnnotation(annoType, annoClass);
                if (found != null){
                    if(a != null){
                        // 注解冲突
                        throw new BeanDefinitionException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    a = found;
                }
            }
        }
        return a;
    }
    // 根据anno类型找anno
    @Nullable
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnotation(Annotation[] annos, Class<A> annoClass){
        for(Annotation anno: annos){
            if(annoClass.isInstance(anno)){
                return (A) anno;
            }
        }
        return null;
    }

    // @Bean的target只有method，所以使用Method获取
    public static String getBeanName(Method method){
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        // @Bean无值，使用方法名为默认值
        if(name.isEmpty()){
            name = method.getName();
        }
        return name;
    }

    // @Component 标注的bean
    public static String getBeanName(Class<?> clazz){
        String name = "";
        Component component = clazz.getAnnotation(Component.class);
        if(component != null){
            name = component.value();
        }else {
            // 在其他注解中查找
            for (Annotation anno: clazz.getAnnotations()){
                // 其他Component类注解中找
                if(findAnnotation(anno.annotationType(), Component.class) != null){
                    try{
                        // 使用value方法获取
                        name = (String) anno.annotationType().getMethod("value").invoke(anno);
                    }catch (ReflectiveOperationException e){
                        throw new BeanDefinitionException("Can't get annotation value", e);
                    }
                }
            }
        }
        if(name.isEmpty()){
            name = clazz.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    @Nullable
    public static Method findAnnotationMethod(Class<?> clazz, Class<? extends Annotation> annoClass){
        List<Method> ms = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annoClass))
                .peek(m -> {
                    if(m.getParameterCount() != 0){
                        throw new BeanDefinitionException(String.format("Method '%s' with @%s must not have argument: %s",
                                m.getName(), annoClass.getSimpleName(), clazz.getName()));
                    }
                }).collect(Collectors.toList());
        if(ms.isEmpty()){
            return null;
        }
        if(ms.size() == 1){
            return ms.get(0);
        }
        throw new NoUniqueBeanDefinitionException(String.format("Multiple methods with @%s found in class %s",
                annoClass.getSimpleName(), clazz.getName()));
    }
    public static Method getNamedMethod(Class<?> clazz, String methodName){
        try{
            return clazz.getDeclaredMethod(methodName);
        }catch (ReflectiveOperationException e){
            throw new BeanDefinitionException(String.format("Method '%s' not found in class: %s", methodName, clazz.getName()));
        }
    }
}
