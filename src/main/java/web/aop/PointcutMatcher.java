package web.aop;

import java.lang.reflect.Method;

/**
 * 判断目标方法是否命中切点表达式。
 *
 * 支持：
 * 1. methodName
 * 2. SimpleClassName#methodName
 * 3. FullyQualifiedClassName#methodName
 */
public final class PointcutMatcher {

    private PointcutMatcher() {
    }

    public static boolean matches(String expression, Class<?> targetClass, Method targetMethod) {
        if (expression == null || expression.isBlank()
                || targetClass == null || targetMethod == null) {
            return false;
        }

        String pointcut = expression.trim();
        int separatorIndex = pointcut.indexOf('#');

        if (separatorIndex < 0) {
            return pointcut.equals(targetMethod.getName());
        }

        if (pointcut.indexOf('#', separatorIndex + 1) >= 0) {
            return false;
        }

        String className = pointcut.substring(0, separatorIndex).trim();
        String methodName = pointcut.substring(separatorIndex + 1).trim();

        if (className.isEmpty() || methodName.isEmpty()) {
            return false;
        }

        boolean classMatched = className.equals(targetClass.getName())
                || className.equals(targetClass.getSimpleName());
        return classMatched && methodName.equals(targetMethod.getName());
    }
}
