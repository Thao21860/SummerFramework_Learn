package org.example.ioc;

import org.example.exception.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

//从注解中提取信息用于创建Bean
public class BeanDefinition implements Comparable<BeanDefinition> {
    // Bean name
    String name;
    // Bean 声明的类型，实际类型可能为其子类，实际类型不必存储，因为可以通过instance.getClass()获得
    Class<?> beanClass;
    Object instance = null;
    Constructor<?> constructor;
    // 工厂所在bean的名字
    String factoryName;
    Method factoryMethod;
    int order;
    // whether annotate @primary
    boolean primary;
    String initMethodName;
    String destroyMethodName;
    Method initMethod;
    Method destroyMethod;

    public BeanDefinition(String name, Class<?> beanClass, Constructor<?> constructor, int order, boolean primary, String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = constructor;
        this.order = order;
        this.primary = primary;
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    public BeanDefinition(String name, Class<?> beanClass, String factoryName, Method factoryMethod, int order, boolean primary, String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = order;
        this.primary = primary;
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getInstance() {
        return instance;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public int getOrder() {
        return order;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public Method getInitMethod() {
        return initMethod;
    }

    public Method getDestroyMethod() {
        return destroyMethod;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Object getRequiredInstance() {
        if (this.instance == null) {
            throw new BeansException(String.format("Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                    this.getName(), this.getBeanClass().getName()));
        }
        return this.instance;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "name='" + name + '\'' +
                ", beanClass=" + beanClass +
                ", instance=" + instance +
                ", constructor=" + constructor +
                ", factoryName='" + factoryName + '\'' +
                ", factoryMethod=" + factoryMethod +
                ", order=" + order +
                ", primary=" + primary +
                ", initMethodName='" + initMethodName + '\'' +
                ", destroyMethodName='" + destroyMethodName + '\'' +
                ", initMethod=" + initMethod +
                ", destroyMethod=" + destroyMethod +
                '}';
    }

    @Override
    public int compareTo(BeanDefinition o) {
        int cmp = Integer.compare(this.order, o.getOrder());
        if(cmp != 0){
            return cmp;
        }
        //无order则名字对比
        return this.name.compareTo(o.getName());
    }
}
