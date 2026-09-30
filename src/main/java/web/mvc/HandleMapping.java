package web.mvc;

import web.ioc.annotation.Type.Controller;
import web.ioc.annotation.Type.RestController;
import web.mvc.annotation.method.DeleteMapping;
import web.mvc.annotation.method.GetMapping;
import web.mvc.annotation.method.PostMapping;
import web.mvc.annotation.method.PutMapping;
import web.mvc.annotation.type.RequestMapping;
import web.mvc.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleMapping {
    //精确匹配的路由:key 格式 "GET:/hello"
    private final Map<String, HandleExecution> mapping = new HashMap<>();
    //含路径参数的动态路由,如 "GET:/user/{id}"
    private final List<UrlPatternEntry> urlPatterns = new ArrayList<>();

    /**
     * 动态路由条目:把 /user/{id} 编译成正则 ^/user/([^/]+)$,并记录参数名
     */
    private static class UrlPatternEntry {
        Pattern pattern;           // 编译后的正则(含 method 前缀,如 "GET:/user/([^/]+)")
        List<String> paramNames;   // ["id"]
        HandleExecution execution;

        UrlPatternEntry(Pattern pattern, List<String> paramNames, HandleExecution execution) {
            this.pattern = pattern;
            this.paramNames = paramNames;
            this.execution = execution;
        }
    }

    public void register(List<Object> beans) {
        for (Object bean : beans) {
            Class<?> aClass = bean.getClass();
            //cglib 代理类是原类的子类,取回原始类才能读到 @Controller/@RestController 注解
            if (aClass.getName().contains("$$EnhancerByCGLIB")) {
                aClass = aClass.getSuperclass();
            }
            boolean present = aClass.isAnnotationPresent(Controller.class)
                    || aClass.isAnnotationPresent(RestController.class);
            if (!present) {
                continue;
            }
            //类级 @RequestMapping 作为 url 前缀
            String preUrl = aClass.isAnnotationPresent(RequestMapping.class)
                    ? aClass.getAnnotation(RequestMapping.class).value() : "";
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    fillMapping(method.getAnnotation(GetMapping.class), bean, method, preUrl);
                } else if (method.isAnnotationPresent(PostMapping.class)) {
                    fillMapping(method.getAnnotation(PostMapping.class), bean, method, preUrl);
                }else if (method.isAnnotationPresent(PutMapping.class)) {
                    fillMapping(method.getAnnotation(PutMapping.class), bean, method, preUrl);
                }else if (method.isAnnotationPresent(DeleteMapping.class)) {
                    fillMapping(method.getAnnotation(DeleteMapping.class), bean, method, preUrl);
                }
            }
        }
    }

    private <T extends Annotation> void fillMapping(T annotation, Object controller, Method method, String preUrl) {
        HandleExecution handleExecution = new HandleExecution(controller, method);
        String url = methodOf(annotation,preUrl);
        //含 {xxx} 的 url → 动态路由;否则精确匹配
        if (url.contains("{")) {
            registerUrlPattern(url, handleExecution);
        } else {
            mapping.put(url, handleExecution);
        }
    }

    /**
     * 注册动态路由:/user/{id} → 正则 + 参数名列表
     */
    private void registerUrlPattern(String url, HandleExecution execution) {
        List<String> paramNames = new ArrayList<>();
        Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(url);
        while (m.find()) {
            paramNames.add(m.group(1));
        }
        String regex = url.replaceAll("\\{[^}]+\\}", "([^/]+)");
        urlPatterns.add(new UrlPatternEntry(Pattern.compile("^" + regex + "$"), paramNames, execution));
    }

    /**
     * 查询路由:先精确匹配,再逐个动态路由正则匹配
     */
    public MappingResult get(String path, RequestMethod requestMethod) {
        String key = requestMethod.name() + ":" + path;
        HandleExecution exact = mapping.get(key);
        if (exact != null) {
            return new MappingResult(exact, Collections.emptyMap());
        }
        for (UrlPatternEntry entry : urlPatterns) {
            Matcher m = entry.pattern.matcher(key);
            if (m.matches()) {
                Map<String, String> pathParams = new HashMap<>();
                for (int i = 0; i < entry.paramNames.size(); i++) {
                    pathParams.put(entry.paramNames.get(i), m.group(i + 1));
                }
                return new MappingResult(entry.execution, pathParams);
            }
        }
        return null;
    }

    /**
     * 统一 url 格式:不管写 /hello 还是 hello,都压成 "/hello",避免双斜杠
     */
    private String normalizeUrl(String preUrl, String value) {
        String u = preUrl + "/" + value;
        u = u.replaceAll("/+", "/");   //多个连续斜杠压成一个
        if (!u.startsWith("/")) {       //开头没有 / 才补上
            u = "/" + u;
        }
        return u;
    }

    public String methodOf(Annotation annotation,String preUrl) {
        if (annotation instanceof GetMapping) {
            GetMapping getMapping = (GetMapping) annotation;
            return getMapping.RequestMethod().getValue() + ":" + normalizeUrl(preUrl, getMapping.value());
        } else if(annotation instanceof PostMapping) {
            PostMapping postMapping = (PostMapping) annotation;
            return postMapping.RequestMethod().getValue() + ":" + normalizeUrl(preUrl, postMapping.value());
        }else if (annotation instanceof PutMapping) {
            PutMapping putMapping = (PutMapping) annotation;
            return putMapping.RequestMethod().getValue() + ":" + normalizeUrl(preUrl, putMapping.value());
        }else if (annotation instanceof DeleteMapping) {
            DeleteMapping deleteMapping = (DeleteMapping) annotation;
            return deleteMapping.RequestMethod().getValue() + ":" + normalizeUrl(preUrl, deleteMapping.value());
        }else{
            return "未知的东西";
        }
    }
}