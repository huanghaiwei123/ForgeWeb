package web.aop;

import lombok.Getter;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class ProceedingJoinPoint {
    private Object target;
    @Getter
    private Method method;
    @Getter
    private Object[] args;
    private MethodProxy methodProxy;

    public ProceedingJoinPoint(Object target, Method method, Object[] args, MethodProxy methodProxy) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.methodProxy = methodProxy;
    }
    public Object proceed() throws Throwable {
        return methodProxy.invokeSuper(target, args);
    }

}
