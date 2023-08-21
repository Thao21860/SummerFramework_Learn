package org.summer.mvc.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.extern.slf4j.Slf4j;
import org.summer.config.PropertyResolver;
import org.summer.exception.NestedRuntimeException;
import org.summer.ioc.AnnotationConfigApplicationContext;
import org.summer.ioc.ApplicationContext;
import org.summer.mvc.utils.WebUtils;

import java.io.IOException;
import java.net.URISyntaxException;
@Slf4j
// 监听servlet容器的启动和销毁
// 在监听到初始化事件时，完成创建IoC容器和注册DispatcherServlet两个工作
public class ContextLoaderListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce)  {
        // 获取servlet容器提供的servletContext
        ServletContext servletContext = sce.getServletContext();
        WebMvcConfiguration.setServletContext(servletContext);
        // 创建ioc容器
        try {
            PropertyResolver pr = WebUtils.createPropertyResolver();
            // 从web.xml获取config配置文件位置
            ApplicationContext applicationContext = createApplicationContext(servletContext.getInitParameter("configuration"), pr);
            // 从ioc容器中查找过滤器，并注册到ServletContext中
            WebUtils.registerFilters(servletContext);
            // 在servletContext中注册dispatcherServlet
            WebUtils.registerDispatcherServlet(servletContext, pr);

            servletContext.setAttribute("applicationContext", applicationContext);

        } catch (IOException e) {
            System.out.println("读取异常");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("查找异常");
            e.printStackTrace();
        } catch (URISyntaxException e){
            System.out.println("URIS");
        } catch (Exception e) {
            System.out.println("其他");
            e.printStackTrace();
        }
    }

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) throws URISyntaxException, IOException, ClassNotFoundException {
        log.info("init ApplicationContext by configuration: {}", configClassName);
        if (configClassName == null || configClassName.isEmpty()) {
            throw new NestedRuntimeException("Cannot init ApplicationContext for missing init param name: configuration");
        }
        Class<?> configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new NestedRuntimeException("Could not load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplicationContext(configClass, propertyResolver);
    }


}
