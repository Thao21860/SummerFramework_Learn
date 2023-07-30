package org.example.mvc.utils;

import org.example.config.PropertyResolver;
import org.example.ioc.ApplicationContextUtils;
import org.example.mvc.web.DispatcherServlet;
import org.example.utils.ClassPathUtils;
import org.example.utils.YamlUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;

public class WebUtils {
//    static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    static final String CONFIG_APP_YAML = "/application.yml";
    static final String CONFIG_APP_PROP = "/test.properties";

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver properyResolver) {
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
}
