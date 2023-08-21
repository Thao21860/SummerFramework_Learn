package org.summer.mvc.web;

import jakarta.annotation.Nullable;
import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Data
public class ModelAndView {
    private String viewName;
    private Map<String, Object> model;

    int status;

    public ModelAndView(String viewName) {
        this(viewName, null, HttpServletResponse.SC_OK);
    }

    public ModelAndView(String viewName, @Nullable Map<String, Object> model) {
        this(viewName, model, HttpServletResponse.SC_OK);

    }

    public ModelAndView(String viewName, @Nullable Map<String, Object> model, int status) {
        this.viewName = viewName;
        this.status = status;
        if (model != null) {
            addModel(model);
        }
    }

    public void addModel(Map<String, Object> map) {
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        this.model.putAll(map);
    }


    public Map<String, Object> getModel(){
        if (this.model == null) {
            this.model = new HashMap<>();
        }
        return this.model;
    }
}
