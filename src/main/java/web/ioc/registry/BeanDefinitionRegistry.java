package web.ioc.registry;

import web.ioc.pojo.BeanDefinition;

import java.util.ArrayList;
import java.util.List;

public class BeanDefinitionRegistry {
    private List<BeanDefinition> beanDefinitions = new ArrayList<>();
    public BeanDefinitionRegistry() {}
    public BeanDefinitionRegistry(List<BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
    }
    public List<BeanDefinition> getBeanDefinitions() {
        return beanDefinitions.stream().toList();
    }
}
