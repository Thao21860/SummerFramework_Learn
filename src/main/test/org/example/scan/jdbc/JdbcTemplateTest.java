package org.example.scan.jdbc;

import org.example.config.PropertyResolver;
import org.example.ioc.*;
import org.example.jdbc.JdbcTemplate;
import org.example.test.ConfigMain;
import org.example.test.jdbc.User;
import org.example.test.postPorcessor.TransactionalTest;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JdbcTemplateTest {
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
        ApplicationContext context = ApplicationContextUtils.getApplicationContext();
        JdbcTemplate template = new JdbcTemplate(context.getBean("dataSource"));
        List<User> list = template.queryForList("select * from account", User.class);
        list.forEach(System.out::println);
    }

    @Test
    public void test2(){
        ApplicationContext context = ApplicationContextUtils.getApplicationContext();
        JdbcTemplate template = new JdbcTemplate(context.getBean("dataSource"));
        String ss = template.queryForObject("select * from account where id = ?",String.class,1);
        System.out.println(ss);
    }
    @Test
    public void transactionTest1(){
        ApplicationContext context = ApplicationContextUtils.getApplicationContext();
        TransactionalTest test = context.getBean("transactionalTest");
        test.add();
    }
    @Test
    public void transactionTest2(){
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ApplicationContextUtils.getApplicationContext();
        TransactionalTest test = context.getBean("transactionalTest");
        BeanDefinition def = context.findBeanDefinition("transactionalTest");
        test.update();
    }
}
