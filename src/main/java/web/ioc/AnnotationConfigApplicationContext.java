package web.ioc;

import web.aop.AopProxy;
import web.aop.annotation.type.Aspect;
import web.core.ClassScanner;
import web.ioc.annotation.Fileld.Autowired;
import web.ioc.annotation.Type.Component;
import web.ioc.annotation.Type.Controller;
import web.ioc.annotation.Type.RestController;
import web.ioc.annotation.Type.Service;
import web.rpc.RpcInvoke;
import web.rpc.ServiceRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AnnotationConfigApplicationContext{
    private final Map<String,Object> beans = new HashMap<>();   //作为容器存储实例对象
    private final Map<String,Object> aspects = new HashMap<>();   //作为容器存储带有@Aspect注解的实例对象
    public AnnotationConfigApplicationContext(String packageName){
        List<Class<?>> scan = ClassScanner.scan(packageName);  //扫描拿到包下的所有类
        List<Class<?>> beanList=fillGetBeans(scan);  //只获取有注解的类
        List<Class<?>> aspectBeans = fillGetAspectBeans(scan);
        registerAspect(aspectBeans);   //先实例化切面类
        register(beanList); //实例化：注册到容器中
        injectDependence();  //对有autowired的字段进行依赖注入
        registerScanForRpc(scan);
    }

    private void registerScanForRpc(List<Class<?>> scan) {
        List<Object> entities = new LinkedList<>();
        for(Class<?> clazz : scan){
            try {
                entities.add(clazz.getDeclaredConstructor().newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        ServiceRegistry registry = new ServiceRegistry(entities);
        RpcInvoke.setBeans(entities);
    }


    private List<Class<?>> fillGetBeans(List<Class<?>> scan) {
        List<Class<?>> beanList = new ArrayList<>();
        for(Class<?> clazz : scan){
            boolean b = clazz.isAnnotationPresent(Component.class)
                    || clazz.isAnnotationPresent(Controller.class)
                    || clazz.isAnnotationPresent(Service.class)
                    || clazz.isAnnotationPresent(RestController.class);
            if(b){
                beanList.add(clazz);
            }
        }
        return beanList;
    }

    private List<Class<?>> fillGetAspectBeans(List<Class<?>> scan) {
        List<Class<?>> beanList = new ArrayList<>();
        for(Class<?> clazz : scan){
            boolean b = clazz.isAnnotationPresent(Aspect.class);
            if(b){
                beanList.add(clazz);
            }
        }
        return beanList;
    }

    private void register(List<Class<?>> beanList){
        AopProxy aopProxy = new AopProxy(getAspects());
        for(Class<?> clazz : beanList){
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();  //拿到当前class对象的无参构造并构建对象实例化
                Object proxy = aopProxy.createProxy(instance);
                String beanName=defaultBeanName(clazz);
                beans.put(beanName, proxy);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void registerAspect(List<Class<?>> aspectBean){
        for(Class<?> clazz : aspectBean){
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();  //拿到当前class对象的无参构造并构建对象实例化
                String beanName=defaultBeanName(clazz);
                aspects.put(beanName, instance);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void injectDependence() {
        for(Object bean : beans.values()){
            for(Field field:getAllFields(bean.getClass())){
                if(field.isAnnotationPresent(Autowired.class)){
                    Object dependency=getFieldDependence(field.getType());
                    if(dependency==null){
                        throw new RuntimeException("找不到类型为 "+field.getType().getName()
                                +" 的Bean,无法注入到 "+bean.getClass().getName()+"#"+field.getName());
                    }
                    field.setAccessible(true);  //私有字段也可以赋值
                    try {
                        field.set(bean,dependency);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    //    为了适应cglib动态代理,子类层层往上找
    private List<Field> getAllFields(Class<?> aClass) {
        List<Field> list = new ArrayList<>();
        while(aClass!=null && Object.class.isAssignableFrom(aClass)){
            Field[] fields = aClass.getDeclaredFields();
            list.addAll(Arrays.stream(fields).toList());
            aClass = aClass.getSuperclass();
        }
        return list;
    }

    /**
     * 获取指定字段的依赖实例
     * @param type
     * @return
     */
    private Object getFieldDependence(Class<?> type) {
        for(Object bean:beans.values()){
            if(type.isAssignableFrom(bean.getClass())){   //type是接口,bean是实现类,isAssignableFrom判断能否赋值
                return bean;
            }
        }
        return null;
    }


    private String defaultBeanName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();   //不带包名
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    public Object getBean(String name) {
        return beans.get(name);
    }

    public List<Object> getBeans() {
        return new ArrayList<>(beans.values());
    }

    public List<Object> getAspects() {
        return new ArrayList<>(aspects.values());
    }
}
