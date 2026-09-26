package web.rpc.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import web.rpc.request.RpcRequest;
import web.rpc.RpcClient;
import web.rpc.responce.RpcResponce;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcProxy {
    public static Object create(Class<?> aclass){
        return create(aclass, "127.0.0.1", 9000);
    }

    public static Object create(Class<?> aclass, String host, int port){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(aclass);
        enhancer.setCallback(
               new MethodInterceptor() {
                   public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                       if (method.getDeclaringClass() == Object.class) {
                           return methodProxy.invokeSuper(o, objects);   //像toString（），hashcode()等object方法直接本地执行
                       }
                       RpcRequest request = new RpcRequest(aclass.getName(), method.getName(),
                               method.getParameterTypes(), objects);
                       RpcResponce response = new RpcClient(host, port).run(request).get(10, TimeUnit.SECONDS);
                       if (!response.getSuccess()) {
                           throw new RuntimeException(response.getErrorMessage());
                       }
                       return response.getData();
                   }
               }
        );
        return enhancer.create();
    }
}
