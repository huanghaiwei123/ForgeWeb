package web.exception.ioc;

import web.aop.AspectRegistry;
import web.core.ClassScanner;
import web.exception.ioc.annotation.Fileld.Autowired;
import web.exception.ioc.factory.BeanFactory;
import web.exception.ioc.factory.DefaultBeanFactory;
import web.ioc.factory.*;
import web.exception.ioc.reader.BeanDefinitionReader;
import web.exception.ioc.registry.BeanDefinitionRegistry;
import web.rpc.RpcInvoke;
import web.rpc.ServiceRegistry;

import java.lang.reflect.Field;
import java.util.*;

public class AnnotationConfigApplicationContext{
    private final List<Class<?>> scan;
    private final Map<String, Object> beans;
    private BeanFactory beanFactory;
    public AnnotationConfigApplicationContext(String packageName){
        scan = ClassScanner.scan(packageName);  //扫描拿到包下的所有类
        beans = getBeans(scan);
        injectDependence();  //对有autowired的字段进行依赖注入
        registerScanForRpc();
    }

    private void registerScanForRpc() {
        List<Object> entities = beans.values().stream().toList();
        ServiceRegistry registry = new ServiceRegistry(entities);
        RpcInvoke.setBeans(entities);
    }


    private Map<String,Object> getBeans(List<Class<?>> scan) {
        BeanFactory beanFactory = getBeanFactory(scan);
        return beanFactory.getBeans();
    }

    private BeanFactory getBeanFactory(List<Class<?>> scan) {
        BeanDefinitionReader reader = new BeanDefinitionReader();
        BeanDefinitionRegistry registry = reader.parseClass(scan);
        AspectRegistry aspectRegistry = reader.parseForAspect(scan);
        BeanFactory beanFactory = new DefaultBeanFactory(registry.getBeanDefinitions());
        beanFactory.setAspect(aspectRegistry.getAspects());
        return beanFactory;
    }

    public List<Object> getBeans() {
        return beans.values().stream().toList();
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
        if(beanFactory==null) {
            beanFactory = getBeanFactory(scan);
        }
        return beanFactory.getBean(type);
    }
}
