package org.example.scan;

import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.test.ConfigT1;
import org.example.test.ContextT1;
import org.example.test.ContextT2;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class ApplicationContextTest {
    @Test
    public void test01() throws URISyntaxException, IOException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));

        PropertyResolver pr = new PropertyResolver(properties);
        AnnotationConfigApplicationContext ap = new AnnotationConfigApplicationContext(ConfigT1.class, pr);
        ContextT1 contextT1 = ap.getBean("contextT1");
        contextT1.getAutoWiredTest();
        ConfigT1 configT1 = ap.getBean("configT1");
        System.out.println(configT1.getKey());
    }

}
