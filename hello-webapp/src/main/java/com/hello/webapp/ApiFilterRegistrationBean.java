package com.hello.webapp;

import org.summer.annotation.Component;
import org.summer.annotation.Order;
import org.summer.mvc.utils.JsonUtils;
import org.summer.mvc.web.FilterRegistrationBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// 过滤器
@Order(200)
@Component
public class ApiFilterRegistrationBean extends FilterRegistrationBean {
    @Override
    public List<String> getUrlPatterns() {
        return Collections.singletonList("/api/*");
    }

    @Override
    public Filter getFilter() {
        return new ApiFilter();
    }
}

class ApiFilter implements Filter {
    // java8 必须重写 init和destroy

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    // 异常捕获filter
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (final Exception e) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            if (!response.isCommitted()) {
                response.reset();
                response.setStatus(400);
                PrintWriter pw = response.getWriter();
                Map<String, Object> map = Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("error", true);
                    put("type", e.getClass().getSimpleName());
                    put("message", e.getMessage() == null ? "" : e.getMessage());
                }});
                JsonUtils.writejson(pw, map );
                pw.flush();
            }
        }
    }
}
