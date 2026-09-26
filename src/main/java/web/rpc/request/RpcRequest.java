package web.rpc.request;

import java.io.Serial;
import java.io.Serializable;

public class RpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID=  1L;
    private String interfaceName; //接口名
    private String methodName; //方法名
    private Class<?>[] parameterTypes;  //参数类型
    private Object[] arguments;  //参数值

    public RpcRequest(String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
    }

    public String getInterfaceName() { return interfaceName; }
    public String getMethodName() { return methodName; }
    public Class<?>[] getParameterTypes() { return parameterTypes; }
    public Object[] getArguments() { return arguments; }
}
