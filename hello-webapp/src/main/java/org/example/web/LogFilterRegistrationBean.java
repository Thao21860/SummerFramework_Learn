package org.example.web;

import org.example.mvc.web.FilterRegistrationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LogFilterRegistrationBean extends FilterRegistrationBean {
    @Override
    public List<String> getUrlPatterns() {
        return Collections.singletonList("/*");
    }

    @Override
    public Filter getFilter() {
        return new LogFilter();
    }
}

class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        logger.info("{}: {}", req.getMethod(), req.getRequestURI());
        chain.doFilter(request, response);
    }
}

