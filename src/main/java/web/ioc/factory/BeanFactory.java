package web.ioc.factory;

import java.util.List;
import java.util.Map;

/**
 * bean创建工厂，负责创建和获取bean
 *
 * 回答两个问题：
 *  1.bean怎么创建
 *  2.bean从哪里获取
 *
 * 采用工厂模式，工厂模式是一种创建型设计模式
 * 核心定义：定义一个用于创建对象的接口，让子类选择实例化哪一个类，工厂方法让一个类的实例化延迟到子类
 * 核心思想：1.封装变化：将“创建什么对象”和“如何创建对象”的逻辑封装起来，当需要新增产品类型的时候。只需拓展工厂和产品类，无需修改现有代码
 *         2.解耦：调用者只依赖抽象产品接口和工厂接口，不直接依赖具体实现类，符合依赖倒置原则
 *
 * 三种常见形态：
 * | 类型 | 说明 | 适用场景 |
 * | :--- | :--- | :--- |
 * | 简单工厂 (Simple Factory) | 一个工厂类根据参数返回不同产品实例（非GoF标准模式） | 产品种类少且固定，逻辑简单 |
 * | 工厂方法 (Factory Method) | 定义创建对象的接口，由子类实现具体创建逻辑 | 产品种类可能扩展，遵循开闭原则 |
 * | 抽象工厂 (Abstract Factory) | 提供创建一系列相关或相互依赖对象的接口 | 需要创建一组相关产品族（如跨平台UI组件） |
 *
 * 优点：
 * 1.符合开闭原则，新增产品无需修改已有代码
 * 2.减低耦合度：客户端面向接口编程
 * 3.统一管理对象创建逻辑，便于维护和测试
 *
 * 缺点：
 * 1.每增加一个产品，需要增加对应的工厂类，导致类数量膨胀
 * 2.增加了系统抽象性和理解复杂度
 */
public interface BeanFactory {
    Object getBean(String beanName);
    <T> Object getBean(String beanName, Class<T> requiredType);
    <T> Object getBean(Class<T> requiredType);
    Map<String,Object> getBeans();
    boolean containsBean(String beanName);
    boolean isSingleton(String beanName);
    boolean isPrototype(String beanName);
}
