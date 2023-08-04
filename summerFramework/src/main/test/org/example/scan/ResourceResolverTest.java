package org.example.scan;

import org.example.utils.YamlUtils;
import org.junit.Test;

import java.util.*;

public class ResourceResolverTest {
    @Test
    public  void test1() {
        Map<String, String> map = System.getenv();
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        Properties pros = new Properties();
        pros.putAll(config);
//        pros.putAll(map);
//        for (String k: config.keySet()){
//            System.out.println(k + "="+ config.get(k));
//        }
        System.out.println("========");
        Set<String> names = pros.stringPropertyNames();
        System.out.println(names);

    }
    @Test
    public  void test2() {
        ClassLoader cl = YamlUtils.class.getClassLoader();
        System.out.println(cl);

        ClassLoader cl2 = ResourceResolverTest.class.getClassLoader();
        System.out.println(cl2);
    }
}
