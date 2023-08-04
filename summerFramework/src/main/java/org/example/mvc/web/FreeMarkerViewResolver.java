package org.example.mvc.web;

import freemarker.cache.WebappTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.*;
import org.example.exception.ServerErrorException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// spring 内置的 FreeMarker 模板引擎
public class FreeMarkerViewResolver implements ViewResolver{
    final String templatePath;
    final String templateEncoding;
    final ServletContext servletContext;
    Configuration config;

    public FreeMarkerViewResolver(ServletContext servletContext, String templatePath, String templateEncoding) {
        this.templatePath = templatePath;
        this.templateEncoding = templateEncoding;
        this.servletContext = servletContext;
    }

    @Override
    public void init() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        cfg.setDefaultEncoding(this.templateEncoding);
        cfg.setTemplateLoader(new WebappTemplateLoader(this.servletContext, this.templatePath));
        // 模板异常处理器
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //设置自动转义策略，这里使用了ENABLE_IF_SUPPORTED_AUTO_ESCAPING_POLICY，它可以根据输出格式自动决定是否启用转义
        cfg.setAutoEscapingPolicy(Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY);
        cfg.setLocalizedLookup(false);

        //创建一个默认的对象包装器，它可以将Java对象转换为freemarker的数据模型
        DefaultObjectWrapper ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_31);
        // 设置是否暴露Java对象的字段，这里启用了暴露字段，表示可以在模板中直接访问Java对象的字段
        ow.setExposeFields(true);
        cfg.setObjectWrapper(ow);
        this.config = cfg;

    }

    @Override
    public void render(String view, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) {
        Template template = null;
        try {
            template = this.config.getTemplate(view);
        } catch (Exception e) {
            throw new ServerErrorException("View not found: " + view);
        }
        try {
            PrintWriter pw = resp.getWriter();
            template.process(model, pw);
            pw.flush();
        } catch (TemplateException | IOException te) {
            throw new ServerErrorException(te);
        }

    }
}

