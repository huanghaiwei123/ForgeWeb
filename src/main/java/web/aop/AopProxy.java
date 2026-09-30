package web.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import web.aop.annotation.method.After;
import web.aop.annotation.method.Before;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 使用cglib而不是jdk自带的代理，因为jdk自带的代理只能代理接口，而cglib能调用asm（字节码操作库）拼出一段目标类的子类的字节码，所以能够代理普通类
 * 注意：cglib不能代理final方法/类，因为cglib代理的原理是生成一个继承父类的子类，final修饰的类无法被继承，cglib直接无法代理整个;方法无法被重写,cglib则拦截不了这个类
 */
public class AopProxy {
    //存储切面示例列表（标了@Apsect那些类）
    private final List<Object> aspects;

    public AopProxy(List<Object> aspects) {
        this.aspects = aspects;
    }


//    核心方法：给目标对象生成代理
    public Object createProxy(Object target) {
        Enhancer enhancer = new Enhancer();  //Enhancer是cglib的“运行时造子类工厂”
        enhancer.setSuperclass(target.getClass());   //给谁造子类
        /**
         * obj是代理对象本身，就是当前被调用的代理实例，目标对象无法被直接拿到
         * method是被调方法的反射对象
         * args是参数
         * proxy是代理对象通往目标对象的桥，cglib 专门给你用来调父类真实实现的东西
         * cglib会拦截所有的方法，但是具体需要执行aop的方法由拦截器自己判断
         */
        enhancer.setCallback((MethodInterceptor)(obj, method, args, proxy)->{
//            目标方法执行之前先遍历看看有没有@Before，有的话就执行前置逻辑
            for(Object aspect : aspects) {
                for(Method ignored :aspect.getClass().getDeclaredMethods()){
                    if(ignored.isAnnotationPresent(Before.class) && ignored.getAnnotation(Before.class).value().equals(method.getName())){
                        ignored.invoke(aspect);
                    }
                }
            }
            //不能用 method.invoke(obj, args),否则会再次触发代理导致无限递归，invokeSuper就是绕过代理直接使用父类的真实方法
            Object result = proxy.invokeSuper(obj, args);
            for(Object aspect : aspects) {
                for(Method ignored :aspect.getClass().getDeclaredMethods()){
                    if(ignored.isAnnotationPresent(After.class) && ignored.getAnnotation(After.class).value().equals(method.getName())){
                        ignored.invoke(aspect);
                    }
                }
            }
            return result;
        });  //参数是拦截器，用于子类方法被调用时，执行什么逻辑

        return enhancer.create();   //真正生成子类实例
    }

}
