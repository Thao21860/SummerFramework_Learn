package org.example.test.postPorcessor;

import org.example.AOP.AnnotationProxyPostProcessor;
import org.example.AOP.ProxyResolver;
import org.example.annotation.Around;
import org.example.annotation.Component;
import org.example.exception.AopConfigException;
import org.example.ioc.ApplicationContextUtils;
import org.example.ioc.BeanDefinition;
import org.example.ioc.BeanPostProcessor;
import org.example.ioc.ConfigurableApplicationContext;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;
@Component
public class AroundProxyBeanPostProcessor extends AnnotationProxyPostProcessor<Around> {

}
