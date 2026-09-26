package web.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务注册表，在启动时初始化一次
 */
public class ServiceRegistry {
    public static final Map<String,Object> services = new HashMap<String,Object>();  //

    public ServiceRegistry() {
    }
    public ServiceRegistry(List<Object> beans) {
        for (Object bean : beans) {
            services.put(bean.getClass().getName(), bean);
        }
    }

}
