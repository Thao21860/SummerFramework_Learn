package org.example.scan;

import org.example.annotation.Import;
import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.BeanDefinition;
import org.example.test.ConfigT1;
import org.example.utils.ClassUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Arrays;

public class AnnotationScanTest {
    @Test
    public void test1() throws URISyntaxException, IOException {
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
}
