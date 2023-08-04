package org.example.scan;

import org.example.annotation.Around;
import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.ioc.BeanPostProcessor;
import org.example.test.ConfigT1;
import org.example.test.ContextT2;
import org.example.test.aop.PoliteInvocationHandler;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class AopTest {
    @Before
    public void before() throws IOException, URISyntaxException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        properties.putAll(config);

        PropertyResolver pr = new PropertyResolver(properties);
        ApplicationContextUtils.setApplicationContext(new AnnotationConfigApplicationContext(ConfigT1.class, pr));
    }
    @Test
    public void getHandler(){
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        PoliteInvocationHandler handler = context.getBean("politeInvocationHandler");
        System.out.println(handler);
    }
    @Test
    public void aroundExist() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("org.example.test.ContextT2");
        Around anno = clazz.getAnnotation(Around.class);
        System.out.println(anno);
    }
    @Test
    public void test01(){
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        ContextT2 contextT2 = context.getBean("contextT2");
        System.out.println(contextT2);
        contextT2.getKey();
    }

    @Test
    public void test02(){
        System.out.println("test02");
        Type type = getClass().getGenericSuperclass();
        System.out.println(type);
        // type 如果是 ParameterizeType 子类，则传入的是参数化泛型。即集成时未指定泛型类型
        System.out.println(type instanceof ParameterizedType);

    }
}
