package com.hello;

import org.summer.annotation.ComponentScan;
import org.summer.annotation.Configuration;
import org.summer.annotation.Import;
import org.summer.jdbc.JdbcConfig;
import org.summer.mvc.web.WebMvcConfiguration;

@Configuration
@ComponentScan
@Import({JdbcConfig.class, WebMvcConfiguration.class})
public class HelloConfiguration {
    public HelloConfiguration() {
        System.out.println("初始化ioc");
    }
}
