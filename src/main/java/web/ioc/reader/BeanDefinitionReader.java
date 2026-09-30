package web.ioc.reader;


import web.aop.AspectRegistry;
import web.aop.annotation.method.After;
import web.aop.annotation.method.Around;
import web.aop.annotation.method.Before;
import web.aop.annotation.type.Aspect;
import web.aop.PointcutMatcher;
import web.ioc.annotation.Fileld.Scope;
import web.ioc.annotation.Type.*;
import web.ioc.enumeration.ScopeEnum;
import web.ioc.pojo.BeanDefinition;
import web.ioc.registry.BeanDefinitionRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BeanDefinitionReader {
    public BeanDefinitionReader() {}

    public BeanDefinitionRegistry parseClass(List<Class<?>> aclazz) {
        Set<String> aspectMethodNames = findAspect(aclazz);
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        for (Class<?> clazz : aclazz) {
            if(clazz.isAnnotationPresent(Service.class)
                    || clazz.isAnnotationPresent(Component.class)
                    || clazz.isAnnotationPresent(Controller.class)
                    || clazz.isAnnotationPresent(RestController.class)
                    || clazz.isAnnotationPresent(Configuration.class)
            ){
                ScopeEnum scopeEnum;
                if(clazz.isAnnotationPresent(Scope.class)){
                    scopeEnum =  clazz.getAnnotation(Scope.class).value() ;
                }else{
                    scopeEnum = ScopeEnum.SINGLETON;
                }
                boolean needProxy = needProxy(clazz, aspectMethodNames);
                BeanDefinition beanDefinition = new BeanDefinition(clazz.getName(), clazz, scopeEnum, needProxy);
                beanDefinitions.add(beanDefinition);
            }
        }
        return new BeanDefinitionRegistry(beanDefinitions);
    }

    public AspectRegistry parseForAspect(List<Class<?>> clazz) {
        List<Object> aspects = new LinkedList<>();
        for (Class<?> aClass : clazz) {
            if(aClass.isAnnotationPresent(Aspect.class)){
                try {
                    aspects.add(aClass.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new AspectRegistry(aspects);
    }



    private Set<String> findAspect(List<Class<?>> aclazz) {
        Set<String> aspectMethodNames = new HashSet<>();
        for (Class<?> clazz : aclazz) {
            if(clazz.isAnnotationPresent(Aspect.class)){
                for(Method method :clazz.getDeclaredMethods()){
                    if(method.isAnnotationPresent(Before.class)){
                        String value = method.getAnnotation(Before.class).value();
                        addAspectMethodName(aspectMethodNames, value);
                    }else if(method.isAnnotationPresent(After.class)){
                        String value = method.getAnnotation(After.class).value();
                        addAspectMethodName(aspectMethodNames, value);
                    }else if(method.isAnnotationPresent(Around.class)){
                        String value = method.getAnnotation(Around.class).value();
                        addAspectMethodName(aspectMethodNames, value);
                    }
                }
            }
            if(clazz.getSuperclass() != null){
                clazz = clazz.getSuperclass();
                for (Method method : clazz.getDeclaredMethods()) {
                    method.setAccessible(true);
                    addAspectMethodName(aspectMethodNames, method.getName());
                }
            }
        }
        return aspectMethodNames;
    }

    private boolean needProxy(Class<?> beanClass, Set<String> aspectMethodNames) {
        for (Method method : beanClass.getDeclaredMethods()) {
            method.setAccessible(true);
            for (String expression : aspectMethodNames) {
                if (PointcutMatcher.matches(expression, beanClass, method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addAspectMethodName(Set<String> aspectMethodNames, String methodName) {
        if (methodName != null && !methodName.isBlank()) {
            aspectMethodNames.add(methodName);
        }
    }

}
