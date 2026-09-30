package web.aop;

import lombok.Getter;

import java.lang.reflect.Method;

public class ProceedingJoinPoint {
    @Getter
    private final Method method;
    @Getter
    private final Object[] args;
    private final Proceeding next;

    public ProceedingJoinPoint(Method method, Object[] args, Proceeding next) {
        this.method = method;
        this.args = args;
        this.next = next;
    }
    public Object proceed() throws Throwable {
        return next.proceed();
    }

}
