package org.example.web;

import org.example.annotation.Component;
import org.example.annotation.Order;
import org.example.mvc.utils.JsonUtils;
import org.example.mvc.web.FilterRegistrationBean;

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
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    // 异常捕获filter
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
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
