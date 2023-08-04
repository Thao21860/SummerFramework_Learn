package org.example.ioc;

import java.util.List;

public interface ApplicationContext extends AutoCloseable{
    // 是否包含指定name的bean
    boolean containsBean(String name);
    // 获取bean by-name
    <T> T getBean(String name);
    // 获取bean by-name and type
    <T> T getBean(String name, Class<T> requiredType);
    // 获取bean by-type
    <T> T getBean(Class<T> requiredType);
    // 获取bean列表 by-type
    <T> List<T> getBeans(Class<T> requiredType);
    // 关闭并执行destroy方法
    void close();
}
