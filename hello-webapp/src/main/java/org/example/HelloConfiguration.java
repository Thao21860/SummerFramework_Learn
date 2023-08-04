package org.example;

import org.example.annotation.Component;
import org.example.annotation.ComponentScan;
import org.example.annotation.Configuration;
import org.example.annotation.Import;
import org.example.mvc.web.WebMvcConfiguration;
import org.example.test.JdbcConfig;

@Configuration
@ComponentScan
@Import({JdbcConfig.class, WebMvcConfiguration.class})
public class HelloConfiguration {
    public HelloConfiguration() {
        System.out.println("初始化ioc");
    }
}
