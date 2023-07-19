package org.example.scan;

import org.example.annotation.Import;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.BeanDefinition;
import org.example.test.ConfigT1;
import org.example.test.ContextT1;
import org.example.test2.ContextT3;
import org.example.utils.ClassUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class AnnotationScanTest {
    @Test
    public void test1() throws URISyntaxException, IOException, ClassNotFoundException {
//        PropertyResolver pr = new PropertyResolver();
        AnnotationConfigApplicationContext ap = new AnnotationConfigApplicationContext(ConfigT1.class,null);
        BeanDefinition t1 = ap.findBeanDefinition("contextT3");
        System.out.println(t1);
    }
    @Test
    public void test2(){
        Class config = ConfigT1.class;
        Import imp = ClassUtils.findAnnotation(config, Import.class);
        System.out.println(Arrays.toString(imp.value()));
    }
    // bean 实例化测试
    @Test
    public void createTest() throws URISyntaxException, IOException, ClassNotFoundException {
        AnnotationConfigApplicationContext ap = new AnnotationConfigApplicationContext(ConfigT1.class,null);
        ContextT1 contextT1 = ap.getBean("contextT1");
        ContextT3 contextT3 = ap.getBean("contextT3");
        System.out.println(contextT1);
        System.out.println(contextT3);
    }
}
