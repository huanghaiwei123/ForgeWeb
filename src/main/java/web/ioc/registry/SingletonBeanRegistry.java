package web.ioc.registry;

import java.util.HashMap;
import java.util.Map;

public class SingletonBeanRegistry {
    private Map<String,Object> singletonBeans = new HashMap<>();
    public SingletonBeanRegistry() {}
    public  SingletonBeanRegistry(Map<String,Object> singletonBeans) {
        this.singletonBeans = singletonBeans;
    }
    public Map<String,Object> getSingletonBeans() {
        return singletonBeans;
    }
    public void addSingletonBeans(Map<String,Object> singletonBeans) {
        this.singletonBeans.putAll(singletonBeans);
    }
}
