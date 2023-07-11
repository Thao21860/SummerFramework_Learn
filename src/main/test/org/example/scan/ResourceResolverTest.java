package org.example.scan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ResourceResolverTest {
    public static void main(String[] args) throws URISyntaxException, IOException {
        ResourceResolver resourceResolver = new ResourceResolver("org.example.scan");
        // lambda表达式 定义mapper
        List<String> list = resourceResolver.scan(res -> {
            String name = res.getName();
            if(name.endsWith(".class")){
                return name.substring(0,name.length() - ".class".length()).replace("/",".").replace("\\",".");
            }
            return  null;
        });
        list.forEach(System.out::println);
    }
}
