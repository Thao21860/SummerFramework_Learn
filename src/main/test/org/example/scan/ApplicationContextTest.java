package org.example.scan;

import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.test.ConfigMain;
import org.example.test.ConfigT1;
import org.example.test.ContextT1;
import org.example.test.ContextT2;
import org.example.utils.YamlUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class ApplicationContextTest {
    @Test
    public void test01() throws URISyntaxException, IOException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        properties.putAll(YamlUtils.loadYamlAsPlainMap());
        PropertyResolver pr = new PropertyResolver(properties);
        AnnotationConfigApplicationContext ap = new AnnotationConfigApplicationContext(ConfigMain.class, pr);
        ContextT1 contextT1 = ap.getBean("contextT1");
        contextT1.getAutoWiredTest();
        ConfigT1 configT1 = ap.getBean("configT1");
        System.out.println(configT1.getKey());
    }

}
