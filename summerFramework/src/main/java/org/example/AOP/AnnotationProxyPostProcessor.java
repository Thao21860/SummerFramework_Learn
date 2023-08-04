package org.example.AOP;

import org.example.annotation.Around;
import org.example.exception.AopConfigException;
import org.example.ioc.ApplicationContextUtils;
import org.example.ioc.BeanDefinition;
import org.example.ioc.BeanPostProcessor;
import org.example.ioc.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
// 实现对不同AOP注解的处理
public abstract class AnnotationProxyPostProcessor<A extends Annotation> implements BeanPostProcessor {
    Map<String, Object> originMap = new HashMap<>();
    // 注解class
    Class<A> annoClass;
    public AnnotationProxyPostProcessor() {
        this.annoClass = getAnnotationClass();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // AOP装配
        Class<?> beanClass = bean.getClass();
        // 查找注解
        A anno = beanClass.getAnnotation(annoClass);
        if(anno != null){
            String handlerName ;
            try{
                handlerName = (String) anno.annotationType().getMethod("value").invoke(anno);
            } catch (ReflectiveOperationException e){
                throw new AopConfigException(e);
            }
            Object proxy = createProxy(beanClass, bean, handlerName);
            originMap.put(beanName, bean);
            return proxy;
        }
        return bean;
    }
    Object createProxy(Class<?> beanClass, Object bean, String handlerName){
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextUtils.getRequiredApplicationContext();
        // 查找拦截器
        BeanDefinition def = ctx.findBeanDefinition(handlerName);
        if(def == null){
            throw new AopConfigException(String.format("can not found invocationHandler%s",handlerName));
        }
        Object handlerBean = def.getInstance();
        if(handlerBean == null){
            // handler可能未实例化，直接调用方法进行实例化
            handlerBean = ctx.createBeanAsEarlySingleton(def);
        }
        if(handlerBean instanceof InvocationHandler){
            InvocationHandler handler = (InvocationHandler) handlerBean;
            return new ProxyResolver().createProxy(bean, handler);
        }else {
            throw new AopConfigException(String.format("%s is not a handler", handlerName));
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originMap.get(beanName);
        if(origin != null){
            return origin;
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    private Class<A> getAnnotationClass() {
        Type type = getClass().getGenericSuperclass();
        if(!(type instanceof ParameterizedType)){
            throw new AopConfigException("do not have definite class");
        }
        ParameterizedType pt = (ParameterizedType) type;
        Type[] args = pt.getActualTypeArguments();
        if(args.length != 1){
            throw new AopConfigException();
        }
        Type r = args[0];
        if(!(r instanceof Class)){
            throw new AopConfigException();
        }
        return (Class<A>) r;
    }
}
