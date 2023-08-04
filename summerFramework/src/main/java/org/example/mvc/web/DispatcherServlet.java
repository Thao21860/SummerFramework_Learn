package org.example.mvc.web;

import io.basc.framework.io.IOUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.annotation.*;
import org.example.config.PropertyResolver;
import org.example.exception.ErrorResponseException;
import org.example.exception.NestedRuntimeException;
import org.example.exception.ServerErrorException;
import org.example.ioc.ApplicationContext;
import org.example.ioc.BeanDefinition;
import org.example.ioc.ConfigurableApplicationContext;
import org.example.mvc.utils.JsonUtils;
import org.example.mvc.utils.PathUtils;
import org.example.mvc.utils.WebUtils;
import org.example.utils.ClassUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    ApplicationContext applicationContext;
    ViewResolver viewResolver;
    // 主页
    String faviconPath;
    String resourcePath;

    List<Dispatcher> getDispatchers = new ArrayList<>();
    List<Dispatcher> postDispatchers = new ArrayList<>();

    public DispatcherServlet() {
    }
    public DispatcherServlet(ApplicationContext requiredApplicationContext, PropertyResolver properyResolver) {
        this.applicationContext = requiredApplicationContext;
        this.viewResolver = applicationContext.getBean(ViewResolver.class);
        this.resourcePath = properyResolver.getProperty("${summer.web.static-path:/static/}");
        this.faviconPath = properyResolver.getProperty("{summer.web.favicon-path:/favicon.ico}");
        if (!this.resourcePath.endsWith("/")) {
            this.resourcePath = this.resourcePath + "/";
        }
    }

    @Override
    public void init() throws ServletException {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.applicationContext;
        List<BeanDefinition> list = context.findBeanDefinitions(Object.class);
        for (BeanDefinition def : list) {
            Class<?> beanClass = def.getBeanClass();
            Object bean = def.getRequiredInstance();
            Controller controller = beanClass.getAnnotation(Controller.class);
            RestController restController = beanClass.getAnnotation(RestController.class);
            if (controller != null && restController != null) {
                throw new ServletException(" Found @Controller and @RestController on class: " + beanClass.getName());
            }
            if (controller != null) {
                addController(false, def.getName(), bean);
            }
            if (restController != null) {
                addController(true, def.getName(), bean);
            }
        }
    }

    void addController(boolean isRest, String name, Object instance) throws ServletException {
        addMethods(isRest, name, instance, instance.getClass());
    }

    void addMethods(boolean isRest, String name, Object instance, Class<?> type) throws ServletException {
        for (Method m : type.getDeclaredMethods()) {
            GetMapping getMapping = m.getAnnotation(GetMapping.class);
            if (getMapping != null) {
                checkMethod(m);
                this.getDispatchers.add(new Dispatcher("GET", isRest, instance, m, getMapping.value()));
            }
            PostMapping postMapping = m.getAnnotation(PostMapping.class);
            if (postMapping != null) {
                checkMethod(m);
                this.getDispatchers.add(new Dispatcher("POST", isRest, instance, m, postMapping.value()));
            }
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
            addMethods(isRest, name, instance, superClass);
        }
    }

    void checkMethod(Method m) throws ServletException {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new ServletException("Can not do URL mapping to static method: " + m);
        }
        m.setAccessible(true);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("process get url ....");
        System.out.println(req.getSession());
        // 查找匹配项
        String url = req.getRequestURI();
        System.out.println(url);
        System.out.println("dispatchers: " );
        this.getDispatchers.forEach(System.out::println);
        if (url.equals(this.faviconPath) || url.startsWith(this.resourcePath)){
            // 处理静态文件
            doResource(url, req, resp);
        } else {
            try {
                doService(url, req, resp, this.getDispatchers);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp, getDispatchers);
    }
    void doService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> getDispatchers) throws ServletException, IOException {
        String url = req.getRequestURI();
        try {
            doService(url, req, resp, getDispatchers);
        } catch (ErrorResponseException e) {
            // 是否已经提交到客户端
            //在处理请求的每一步都可以向HttpServletResponse写入响应，因此，后续步骤写入时，应判断前面的步骤是否已经写入并发送了HTTP Header。isCommitted()方法就是干这个用的：
            if (!resp.isCommitted()) {
                resp.resetBuffer();
                resp.sendError(e.statusCode);
            }
        } catch (RuntimeException | ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new NestedRuntimeException(e);
        }
    }

    void doService(String url, HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> getDispatchers) throws Exception {
        for (Dispatcher dispatcher : getDispatchers) {
            Result result = dispatcher.process(url, req, resp);
            System.out.println("processed: " + result.processed);
            if (result.processed) {
                Object r = result.returnObject;
                if (dispatcher.isRest) {
                    // 有返回结果
                    if (!resp.isCommitted()) {
                        resp.setContentType("application/json");
                    }
                    if (dispatcher.isResponseBody) {
                        if (r instanceof String ) {
                            String s = (String) r;
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (r instanceof byte[]) {
                            byte[] data = (byte[]) r;
                            ServletOutputStream outputStream = resp.getOutputStream();
                            outputStream.write(data);
                            outputStream.flush();
                        } else {
                            throw new ServletException("unable to process REST result when handle url" + url);
                        }
                    } else if (!dispatcher.isVoid) {
                        PrintWriter pw = resp.getWriter();
                        JsonUtils.writejson(pw,r);
                        pw.flush();
                    }
                } else {
                    if (!resp.isCommitted()) {
                        resp.setContentType("text/html");
                    }
                    if (r instanceof String) {
                        String s = (String) r;
                        if (dispatcher.isResponseBody) {
                            PrintWriter pw = resp.getWriter();
                            pw.write(s);
                            pw.flush();
                        } else if (s.startsWith("redirect:")) {
                            resp.sendRedirect(s.substring("redirect".length()));
                        } else {
                            throw new ServletException("unable process String result when handle url:" + url);
                        }
                    } else if (r instanceof byte[]) {
                        if (dispatcher.isResponseBody){
                            byte[] data = (byte[]) r;
                            ServletOutputStream outputStream = resp.getOutputStream();
                            outputStream.write(data);
                            outputStream.flush();
                        }else {
                            throw new ServletException("unable process byte[] result when handle url:" + url);
                        }

                    } else if (r instanceof ModelAndView) {
                        ModelAndView mv = (ModelAndView) r;
                        String view = mv.getViewName();
                        if (view.startsWith("redirect:")) {
                            resp.sendRedirect(view.substring("redirect:".length()));
                        } else {
                            this.viewResolver.render(view, mv.getModel(), req, resp);
                        }
                    } else if (!dispatcher.isVoid && r != null) {
                        throw new ServletException("unable process" + r.getClass().getName() + "result when handle url:" + url);
                    }
                }
            }
        }
    }

    void doResource(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = req.getServletContext();
        try (InputStream input = ctx.getResourceAsStream(url)) {
            if (input == null) {
                resp.sendError(404,"Not Found!");
            } else {
                // 获取访问要获取的文件名
                String file = url;
                int n = url.lastIndexOf('/');
                if (n > 0 ) {
                    file = url.substring(n + 1);
                }
                //MIME类型是一种在互联网通信过程中定义的一种文件数据类型，用来表示文件的内容和格式。MIME类型的格式是大类型/小类型，比如text/html表示HTML文本，image/jpeg表示JPEG图片
                String mime = ctx.getMimeType(file);
                if (mime == null) {
                    // 传输二进制格式
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);
                ServletOutputStream output = resp.getOutputStream();
                // input 数据复制到 output
                IOUtils.copy(input, output);
                output.flush();
            }
        }
    }

    static class Dispatcher {
        final static Result NOT_PROCESSED = new Result(false, null);
        boolean isRest;
        // 是否标注@ResponseBody
        boolean isResponseBody;
        boolean isVoid;
        // url正则匹配
        Pattern urlPattern;
        // bean实列
        Object controller;
        // 对应的处理方法
        Method handlerMethod;
        // 方法参数
        Param[] methodParameters;

        public Dispatcher(String httpMethod,boolean isRest, Object controller, Method handlerMethod, String urlPattern) throws ServletException {
            this.isRest = isRest;
            this.isResponseBody = handlerMethod.getAnnotation(ResponseBody.class) != null;
            this.isVoid = handlerMethod.getReturnType() == void.class;
            this.urlPattern = PathUtils.compile(urlPattern);
            this.controller = controller;
            this.handlerMethod = handlerMethod;
            Parameter[] parameters = handlerMethod.getParameters();
            Annotation[][] annotations = handlerMethod.getParameterAnnotations();
            this.methodParameters = new Param[parameters.length];
            for (int i = 0 ; i < parameters.length; i++) {
                this.methodParameters[i] = new Param(handlerMethod, handlerMethod, parameters[i], annotations[i]);
            }
        }
        // process 便利匹配 servlet
        public Result process(String url, HttpServletRequest req, HttpServletResponse resp) throws Exception {
            Matcher matcher = this.urlPattern.matcher(url);
            System.out.println("matcher:"+ matcher.matches() + "  " +  this.urlPattern + "  " + url);
            if (matcher.matches()) {
                Object[] arguments = new Object[this.methodParameters.length];
                for (int i = 0; i < this.methodParameters.length; i ++) {
                    Param param = methodParameters[i];
                    if (param.paramType == ParamType.PATH_VARIABLE){
                        arguments[i] = PathVariable(param, matcher);
                    } else if (param.paramType == ParamType.REQUEST_BODY) {
                        arguments[i] = RequestBody(req, param);
                    } else if (param.paramType == ParamType.REQUEST_PARAM) {
                        arguments[i] = RequestParam(req,param);
                    } else if (param.paramType == ParamType.SERVLET_VARIABLE){
                        arguments[i] = ServletVariable(req, resp, param);
                    }
                }
                Object result = null;
                try {
                    result = this.handlerMethod.invoke(this.controller, arguments);
                } catch (InvocationTargetException e){
                    Throwable t = e.getCause();
                    if (t instanceof Exception) {
                        Exception ex = (Exception) t;
                        throw  ex;
                    }
                    throw e;
                }
                return new Result(true, result);
            }
            return NOT_PROCESSED;
        }

        Object PathVariable(Param param,  Matcher matcher){
            try {
                String s = matcher.group(param.name);
                return convertToType(param.classType, s);
            } catch (IllegalArgumentException e) {
                throw new ServerErrorException("Path variable " + param.name + "not found");
            }
        }

        Object RequestBody(HttpServletRequest req, Param param)  {
            try{
                BufferedReader reader = req.getReader();
                return JsonUtils.readJson(reader, param.classType);
            } catch (IOException e) {
                throw new ServerErrorException();
            }
        }

        Object RequestParam(HttpServletRequest request, Param param){
            String s = getOrDefault(request, param.name, param.defaultValue);
            return convertToType(param.classType, s);
        }

        Object ServletVariable(HttpServletRequest request,HttpServletResponse response,Param param){
            Class<?> classType = param.classType;
            if (classType == HttpServletRequest.class) {
                return request;
            } else if (classType == HttpServletResponse.class) {
                return response;
            } else if (classType == HttpSession.class) {
                return request.getSession();
            } else if (classType == ServletContext.class) {
                return request.getServletContext();
            } else {
                throw new ServerErrorException("Could not determine argument type: " + classType);
            }
        }

        String getOrDefault(HttpServletRequest request, String name, String defaultValue){
            String s = request.getParameter(name);
            if (s == null) {
                if (WebUtils.DEFAULT_PARAM_VALUE.equals(defaultValue)) {
                    throw new ServerErrorException("Request parameter '" + name + "' not found.");
                }
                return defaultValue;
            }
            return s;
        }
        Object convertToType(Class<?> classType, String s) {
            if (classType == String.class) {
                return s;
            } else if (classType == boolean.class || classType == Boolean.class) {
                return Boolean.valueOf(s);
            }else if (classType == int.class || classType == Integer.class) {
                return Integer.valueOf(s);
            }else if (classType == long.class || classType == Long.class) {
                return Long.valueOf(s);
            }else if (classType == byte.class || classType == Byte.class) {
                return Byte.valueOf(s);
            }else if (classType == short.class || classType == Short.class) {
                return Short.valueOf(s);
            }else if (classType == float.class || classType == Float.class) {
                return Float.valueOf(s);
            }else if (classType == double.class || classType == Double.class) {
                return Double.valueOf(s);
            }else {
                throw new ServerErrorException("Could not determine argument type" + classType);
            }
        }
    }

    static enum ParamType {
        PATH_VARIABLE,
        REQUEST_PARAM,
        REQUEST_BODY,
        SERVLET_VARIABLE;
    }

    static class Param {
        String name;
        ParamType paramType;
        // 参数Class类型
        Class<?> classType;
        // 默认参数
        String defaultValue;

        public Param(Method handlerMethod, Method handlerMethod1, Parameter parameter, Annotation[] annotation) throws ServletException {
            PathVariable pv = ClassUtils.getAnnotation(annotation, PathVariable.class);
            RequestParam rp = ClassUtils.getAnnotation(annotation, RequestParam.class);
            RequestBody rb = ClassUtils.getAnnotation(annotation, RequestBody.class);
            // 只能有一个
            int total = (pv == null ? 0:1) + (rp == null ? 0:1) + (rb == null ? 0:1);
            if (total > 1) {
                throw new ServletException("Annotation of Param can not combined at method:" + handlerMethod);
            }
            this.classType = parameter.getType();
            if (pv != null) {
                this.name = pv.value();
                this.paramType = ParamType.PATH_VARIABLE;
            } else if (rp != null) {
                this.name = rp.value();
                this.paramType = ParamType.REQUEST_PARAM;
            } else if (rb != null) {
                this.paramType = ParamType.REQUEST_BODY;
            } else {
                this.paramType = ParamType.SERVLET_VARIABLE;
                if (this.classType != HttpServletRequest.class && this.classType != HttpServletResponse.class && this.classType != HttpSession.class
                && this.classType != ServletContext.class) {
                    // 不支持的类型
                    throw new ServletException("(Missing annotation?) Unsupported argument type: " + this.classType + " at method: " + handlerMethod);
                }
            }


        }
        @Override
        public String toString() {
            return "Param [name=" + name + ", paramType=" + paramType + ", classType=" + classType + ", defaultValue=" + defaultValue + "]";
        }
    }
    
    @Data
    @AllArgsConstructor
    static class Result {
        boolean processed;
        Object returnObject;
    }
}


