package org.summer.ioc;

import jakarta.annotation.Nullable;

import java.util.List;

// 框架使用接口，对BeanDefinition进行操作并实现Bean扫描创建
public interface ConfigurableApplicationContext extends ApplicationContext{
    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> type);

    // 创建bean实例
    Object createBeanAsEarlySingleton(BeanDefinition def);
}
