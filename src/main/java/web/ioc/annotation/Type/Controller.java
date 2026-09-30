package web.ioc.annotation.Type;

import java.lang.annotation.*;

@Target(ElementType.TYPE)    //只能标在类/接口/枚举上
@Retention(RetentionPolicy.RUNTIME)    //运行时保留，反射才读得到
@Inherited
public @interface Controller {

}
