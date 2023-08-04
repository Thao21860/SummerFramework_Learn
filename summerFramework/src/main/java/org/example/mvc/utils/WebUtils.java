package org.example.mvc.utils;

import org.example.config.PropertyResolver;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.mvc.web.DispatcherServlet;
import org.example.mvc.web.FilterRegistrationBean;
import org.example.utils.ClassPathUtils;
import org.example.utils.YamlUtils;

import javax.servlet.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

public class WebUtils {
//    static final Logger logger = LoggerFactory.getLogger(WebUtils.class);
    public static final String DEFAULT_PARAM_VALUE = "\0\t\0\t\0";

    static final String CONFIG_APP_YAML = "/application.yml";
    static final String CONFIG_APP_PROP = "/test.properties";

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver properyResolver) {
        // 创建DispatcherServlet并传入ApplicationContext
        DispatcherServlet dispatcherServlet = new DispatcherServlet(ApplicationContextUtils.getRequiredApplicationContext(), properyResolver);
        ServletRegistration.Dynamic dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
    }

    /**
     * Try load property resolver from /application.yml or /application.properties.
     */
    public static PropertyResolver createPropertyResolver() throws IOException {
        final Properties props = new Properties();
        props.load(WebUtils.class.getClassLoader().getResourceAsStream("test.properties"));
        // try load application.yml, or application.properties
        try {
            Map<String, Object> ymlMap = YamlUtils.loadYamlAsPlainMap();
            for (String key : ymlMap.keySet()) {
                Object value = ymlMap.get(key);
                if (value instanceof String ) {
                    String strValue = (String) value;
                    props.put(key, strValue);
                }
            }
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // try load application.properties:
                ClassPathUtils.readInputStream(CONFIG_APP_PROP, (input) -> {
                    props.load(input);
                    return true;
                });
            }
        }
        return new PropertyResolver(props);
    }

    public static void registerFilters(ServletContext servletContext) {
        ApplicationContext applicationContext = ApplicationContextUtils.getRequiredApplicationContext();
        for (FilterRegistrationBean filterRegBean : applicationContext.getBeans(FilterRegistrationBean.class)) {
            List<String> urlPatterns = filterRegBean.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.isEmpty()) {
                throw new IllegalArgumentException("No url patterns for {}" + filterRegBean.getClass().getName());
            }
            Filter filter = Objects.requireNonNull(filterRegBean.getFilter(),"FilterRegistrationBean.getFilter don not return null");
            FilterRegistration.Dynamic filterReg = servletContext.addFilter(filterRegBean.getName(), filter);
            // 设置过滤器需要过滤的url， 过滤的请求类型， true表示后匹配，false为前匹配，后匹配的过滤器会在前匹配的过滤器之后执行。
            filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),true, urlPatterns.toArray(new String[0]));
        }
    }
}
