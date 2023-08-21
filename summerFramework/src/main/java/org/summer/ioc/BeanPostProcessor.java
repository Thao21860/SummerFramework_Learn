package org.summer.ioc;

public interface BeanPostProcessor {
    // 创建bean后执行
    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }
    // 获取原始bean时执行
    default Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }
    // 修改属性时执行
    default Object postProcessOnSetProperty(Object bean, String beanName){
        return bean;
    }

}
