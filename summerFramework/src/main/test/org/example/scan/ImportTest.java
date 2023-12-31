package org.example.scan;

import org.example.annotation.Component;
import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.utils.ClassUtils;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class ImportTest {
    public ImportTest() throws IOException, URISyntaxException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        properties.putAll(config);

        PropertyResolver pr = new PropertyResolver(properties);
        ApplicationContextUtils.setApplicationContext(new AnnotationConfigApplicationContext(ConfigT1.class, pr));
    }
    @Test
    public void test1(){
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        ContextT3 contextT3 = context.getBean("contextT3");
        System.out.println(contextT3);
    }
    @Test
    public void test2(){
        Class<?> clazz = ConfigT1.class;
        Component component = ClassUtils.findAnnotation(clazz, Component.class);
        System.out.println(component == null);

    }
    // 未使用configuration
    @Test
    public void test3(){
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        ContextT1 contextT1 = context.getBean("getT1");
        System.out.println(contextT1);

    }

}
