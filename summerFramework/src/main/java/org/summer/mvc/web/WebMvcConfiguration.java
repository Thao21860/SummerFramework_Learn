package org.summer.mvc.web;

import org.summer.annotation.AutoWired;
import org.summer.annotation.Bean;
import org.summer.annotation.Configuration;
import org.summer.annotation.Value;

import javax.servlet.ServletContext;
import java.util.Objects;

@Configuration
public class WebMvcConfiguration {
    private static ServletContext servletContext = null;

    public static void setServletContext(ServletContext servletContext) {
        WebMvcConfiguration.servletContext = servletContext;
    }

    @Bean(initMethod = "init")
    public ViewResolver viewResolver (
            @AutoWired ServletContext servletContext,
            @Value("${summer.web.freemarker.template-path:/WEB-INF/templates}") String templatePath,
            @Value("${summer.web.freemarker.template-encoding:UTF-8}") String templateEncoding){
        return new FreeMarkerViewResolver(servletContext, templatePath, templateEncoding);
    }
    // ServletContext 由 servlet容器提供不在ioc容器中，需要添加
    @Bean
    public ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
    }
}
