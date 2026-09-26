package web.rpc;

import web.rpc.request.RpcRequest;
import web.rpc.responce.RpcResponce;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
public class RpcInvoke {
    private static List<Object> beans;  //实例化容器

    public static void setBeans(List<Object> beans) {
        RpcInvoke.beans = beans;
    }

    public RpcResponce invoke(RpcRequest res){
        Object bean = null;
        String interfaceName = res.getInterfaceName();
        String methodName = res.getMethodName();
        Class<?>[] parameterTypes = res.getParameterTypes();
        Object[] arguments = res.getArguments();
        for(Object obj : beans){
            if(interfaceName.equals(obj.getClass().getName())){
                bean=obj;
                break;
            }
        }
        if (bean == null) {
            return new RpcResponce(false, null, "service not found: " + interfaceName);
        }
        Method method = null;
        for (Method candidate : bean.getClass().getDeclaredMethods()) {
            if (methodName.equals(candidate.getName())
                    && Arrays.equals(candidate.getParameterTypes(), parameterTypes)) {
                method = candidate;
                break;
            }
        }
        if (method == null) {
            return new RpcResponce(false, null, "method not found: " + methodName);
        }
        method.setAccessible(true);
        try {
            Object result = method.invoke(bean, arguments);
            return new RpcResponce(true,result,null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException && ((InvocationTargetException) e).getCause() != null
                    ? ((InvocationTargetException) e).getCause() : e;
            return new RpcResponce(false,null,cause.getMessage());
        }
    }

}
