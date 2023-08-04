package org.example.test.aop;

import org.example.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
@Component
public class PoliteInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Hello world!!!");
        return method.invoke(proxy,args);
    }
}
