package web.ioc.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import web.ioc.enumeration.ScopeEnum;

@Data
@AllArgsConstructor
public class BeanDefinition {
    /**
     * bean名称
     */
    private String beanName;   //bean名称

    /**
     *  bean类型
     */
    private Class<?> beanClass;

    /**
     * bean作用域，包含：
     *  SINGLETON，单例，只创建一次，以后每次需要从缓存中拿取
     *  PROTOTYPE，每次需要就重新创建
     */
    private ScopeEnum scopeEnum;

    /**
     * 是否需要代理
     */
    private Boolean isNeedProxy;
}
