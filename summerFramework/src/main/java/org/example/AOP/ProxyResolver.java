package org.example.AOP;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyResolver {
    ByteBuddy byteBuddy = new ByteBuddy();
    // 传入bean和拦截器 创建代理
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T bean, InvocationHandler handler){
        Class<?> targetClass = bean.getClass();
        // byteBuddy动态代理
        Class<?> proxyClass = this.byteBuddy.subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                .method(ElementMatchers.isPublic())
                .intercept(InvocationHandlerAdapter.of(
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                return handler.invoke(bean,method,args);
                            }
                        }
                ))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();
        Object proxy;
        try{
            proxy = proxyClass.getConstructor().newInstance();
        }catch (RuntimeException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }
}
