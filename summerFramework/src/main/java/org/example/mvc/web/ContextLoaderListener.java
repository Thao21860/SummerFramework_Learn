package org.example.mvc.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.mvc.utils.WebUtils;
import org.example.mvc.webapp.WebAppConfig;
import org.example.test.ConfigT1;
import org.example.utils.YamlUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

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
            ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Class.forName(servletContext.getInitParameter("configuration")), pr);
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

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver){
        return null;
    }


}
