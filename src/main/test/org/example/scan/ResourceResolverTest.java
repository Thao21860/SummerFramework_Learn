package org.example.scan;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.example.DI.YamlUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class ResourceResolverTest {
    public static void main(String[] args) {
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
}
