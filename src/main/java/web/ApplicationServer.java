package web;

import web.ioc.AnnotationConfigApplicationContext;
import web.mvc.HandleMapping;
import web.mvc.Dispatcher;
import web.mvc.TypeConverter;
import web.server.HttpServer;
import java.util.List;

public class ApplicationServer {
    public static void run(Class<?> primaryClass) {
        String packageName = primaryClass.getPackage().getName();
        //1.扫描 test 包,创建 IOC 容器(注册所有带注解的类)
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(packageName);
        //2.注册路由:遍历容器里的 bean,把 @GetMapping/@PostMapping 的 url 映射到方法
        HandleMapping handleMapping = new HandleMapping();
        List<Object> beans = context.getBeans();
        handleMapping.register(beans);
        TypeConverter.setBeans(beans);
        //3.创建分发器,启动 Netty
        Dispatcher dispatcher = new Dispatcher(handleMapping);
        HttpServer httpServer = new HttpServer(8080, dispatcher);
        try {
            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}