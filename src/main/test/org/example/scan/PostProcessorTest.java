package org.example.scan;

import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.test.ConfigT1;
import org.example.test.ContextT2;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class PostProcessorTest {
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
    public void Test1(){
        ApplicationContext context = ApplicationContextUtils.getApplicationContext();
        ContextT2 contextT2 = context.getBean("contextT2");
        System.out.println(contextT2);
    }
//    @Test
    public void test2() throws ClassNotFoundException {
        Class<?> a = Class.forName("org.example.ioc.BeanPostProcessor");
        Class<?> b = Class.forName("org.example.test.PostProcessorOne");
        Class<?> c = Class.forName("org.example.ioc.BeanDefinition");
        System.out.println(a.isAssignableFrom(c));

    }

}
