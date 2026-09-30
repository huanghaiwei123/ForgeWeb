package web.ioc.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import web.aop.AopProxy;
import web.ioc.enumeration.ScopeEnum;
import web.ioc.exception.NoSuchBeanException;
import web.ioc.exception.NoUniqueBeanException;
import web.ioc.pojo.BeanDefinition;
import web.ioc.registry.SingletonBeanRegistry;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class DefaultBeanFactory implements BeanFactory {

    private Map<String,Object> beans = new LinkedHashMap<>();

    private Map<String,Object> singletonBeans = new LinkedHashMap<>();

    private List<BeanDefinition> beanDefinitions = new ArrayList<>();

    private List<Object> aspects;

    @Getter
    private SingletonBeanRegistry singletonBeanRegistry;

    private ObjectMapper om = new ObjectMapper();

    public DefaultBeanFactory(List<BeanDefinition> definitions)  {
        this.beanDefinitions.addAll(definitions);
        for (BeanDefinition definition : definitions) {
            String beanName = definition.getBeanName();
            Class<?> aClass = definition.getBeanClass();
            Boolean need = definition.getIsNeedProxy();
            ScopeEnum scopeEnum = definition.getScopeEnum();
            String simpleName = aClass.getSimpleName();
            try {
               Object bean= Class.forName(beanName).getDeclaredConstructor().newInstance();
               if (need) {
                   AopProxy aopProxy = new AopProxy(aspects);
                   bean = aopProxy.createProxy(bean);
               }
               if (scopeEnum == ScopeEnum.SINGLETON) {
                   singletonBeans.put(beanName, bean);
               }
               beans.put(beanName,bean);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        singletonBeanRegistry = new SingletonBeanRegistry(singletonBeans);
    }

    public void setAspect(List<Object> aspects) {
        this. aspects = aspects;
    }

    /**
     * 非单例对象重新创建
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        Object bean = beans.get(beanName);
        if (bean == null) {
            throw new NoSuchBeanException(beanName);
        }
        if(isPrototype(beanName)) {
            try {
                bean = bean.getClass().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    @Override
    public <T> Object getBean(String beanName, Class<T> requiredType) {
        Object bean = beans.get(beanName);
        if (bean != null && requiredType.isAssignableFrom(bean.getClass())) {
            return bean;
        }
       return getBean(beanName);
    }

    @Override
    public <T> Object getBean(Class<T> requiredType) {
        Collection<Object> values = beans.values();
        List<Object> beans = new ArrayList<>();
        for (Object value : values) {
            if (requiredType.isAssignableFrom(value.getClass())) {
                beans.add(value);
            }
        }
        if (beans.isEmpty()) {
            throw new NoSuchBeanException(requiredType.getName());
        } else if (beans.size() > 1) {
            throw new NoUniqueBeanException(requiredType.getName());
        }
        Object bean = beans.get(0);
        return isSingleton(requiredType.getName()) ? (T) bean : (T)getBean(requiredType.getName());
    }

    @Override
    public Map<String, Object> getBeans() {
        return Map.copyOf(beans);
    }

    @Override
    public boolean containsBean(String beanName) {
        return beans.containsKey(beanName);
    }

    @Override
    public boolean isSingleton(String beanName) {
        if(beans.get(beanName) == null){
            throw new NoSuchBeanException(beanName);
        }
        return singletonBeans.containsKey(beanName);
    }

    @Override
    public boolean isPrototype(String beanName) {
        if(beans.get(beanName) == null){
            throw new NoSuchBeanException(beanName);
        }
        return !isSingleton(beanName);
    }

}
