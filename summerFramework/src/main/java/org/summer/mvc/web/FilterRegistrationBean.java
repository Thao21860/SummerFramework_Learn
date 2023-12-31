package org.summer.mvc.web;

import javax.servlet.Filter;
import java.util.List;

// 过滤器bean抽象工厂类
public abstract class FilterRegistrationBean {
    public abstract List<String> getUrlPatterns();

    public String getName() {
        String name = getClass().getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        if (name.endsWith("FilterRegistrationBean") && name.length() > "FilterRegistrationBean".length()) {
            return name.substring(0,name.length() - "FilterRegistrationBean".length());
        }
        if (name.endsWith("FilterRegistration") && name.length() > "FilterRegistration".length()) {
            return name.substring(0,name.length() - "FilterRegistration".length());
        }
        return name;
    }

    public abstract Filter getFilter();
}
