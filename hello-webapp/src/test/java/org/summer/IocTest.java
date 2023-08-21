package org.summer;

import com.hello.HelloConfiguration;
import org.summer.config.PropertyResolver;
import org.summer.ioc.AnnotationConfigApplicationContext;
import org.summer.ioc.ApplicationContext;
import org.summer.mvc.utils.WebUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class IocTest {
    @Test
    public void test01() throws IOException, URISyntaxException, ClassNotFoundException {
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(HelloConfiguration.class,propertyResolver);
        applicationContext.getBean("jdbcConfig");
    }
    @Test
    public void test02() throws ClassNotFoundException {
        Class<?> clazz = Class.forName("org.summer.mvc.web.WebMvcConfiguration");

    }

}
