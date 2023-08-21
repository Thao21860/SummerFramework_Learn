package org.summer.mvc.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

// 处理ModelAndView的模板引擎
public interface ViewResolver {
    void init();

    void render(String view, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp);
}
