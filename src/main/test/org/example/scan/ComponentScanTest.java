package org.example.scan;


import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.test.ConfigMain;
import org.example.test.ConfigT1;
import org.example.test2.ContextT3;
import org.example.test2.ContextT4;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class ComponentScanTest {
    @Before
    public void before() throws IOException, URISyntaxException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        properties.putAll(config);

        PropertyResolver pr = new PropertyResolver(properties);
        ApplicationContextUtils.setApplicationContext(new AnnotationConfigApplicationContext(ConfigMain.class, pr));
    }
    @Test
    public void test1(){
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        ContextT3 contextT3 = context.getBean("contextT3");
        System.out.println(contextT3);
    }
//    @Test
    public void test2() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("org.example.test2.ContextT4");
        System.out.println(clazz);
    }
}
