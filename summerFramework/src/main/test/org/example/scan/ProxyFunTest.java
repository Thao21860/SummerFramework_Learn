package org.example.scan;

import org.example.AOP.ProxyResolver;
import org.example.test.ioc.ContextT2;
import org.example.test.aop.PoliteInvocationHandler;
import org.junit.Test;

public class ProxyFunTest {
    @Test
    public void test01(){
        ContextT2 contextT2 = new ContextT2();
        ProxyResolver proxyResolver = new ProxyResolver();
        ContextT2 proxy = proxyResolver.createProxy(contextT2,new PoliteInvocationHandler());
        System.out.println(proxy.getKey());

    }
    @Test
    public void test02() throws NoSuchMethodException {
        ContextT2 contextT2 = new ContextT2();
        System.out.println(contextT2.getClass().getConstructor());
    }
}
