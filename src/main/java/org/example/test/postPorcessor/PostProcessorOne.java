package org.example.test.postPorcessor;

import org.example.annotation.Component;
import org.example.ioc.BeanPostProcessor;
import org.example.test.ContextT2;

import java.util.HashMap;
import java.util.Map;

@Component
// 处理context2 代理
public class PostProcessorOne implements BeanPostProcessor {
    Map<String ,Object> originMap = new HashMap<>();
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if(ContextT2.class.isAssignableFrom(bean.getClass())){
            ContextT2Proxy contextT2Proxy = new ContextT2Proxy((ContextT2) bean);
            // 保存原始bean
            originMap.put(beanName,bean);
            return contextT2Proxy;
        }
        return bean;
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

}
class ContextT2Proxy extends ContextT2{
    final ContextT2 contextT2;

    public ContextT2Proxy(ContextT2 contextT2) {
        this.contextT2 = contextT2;
    }
}
