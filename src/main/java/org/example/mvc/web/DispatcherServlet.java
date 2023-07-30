package org.example.mvc.web;


import org.example.config.PropertyResolver;
import org.example.ioc.ApplicationContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DispatcherServlet extends HttpServlet {
    public DispatcherServlet() {
    }
    public DispatcherServlet(ApplicationContext requiredApplicationContext, PropertyResolver properyResolver) {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter pw = resp.getWriter();
        pw.write("<h1> hello world! </h1>");
        pw.flush();
    }
}
