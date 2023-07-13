package org.example.scan;

import org.example.DI.PropertyResolver;
import org.example.DI.YamlUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.*;

public class PropertyResolverTest {
    @Test
    public void test1() {
        Properties props  = new Properties();
        props.setProperty("app.title", "Summer Framework");
        props.setProperty("app.version", "v1.0");
        props.setProperty("jdbc.url", "jdbc:mysql://localhost:3306/simpsons");
        props.setProperty("jdbc.username", "bart");
        props.setProperty("jdbc.password", "51mp50n");
        props.setProperty("jdbc.pool-size", "20");
        props.setProperty("jdbc.auto-commit", "true");
        props.setProperty("scheduler.started-at", "2023-03-29T21:45:01");
        props.setProperty("scheduler.backup-at", "03:05:10");
        props.setProperty("scheduler.cleanup", "P2DT8H21M");

        PropertyResolver pr = new PropertyResolver(props);
        assertEquals("Summer Framework", pr.getProperty("app.title"));
        assertEquals("v1.0", pr.getProperty("app.version"));
        assertEquals("v1.0", pr.getProperty("app.version", "unknown"));
        assertNull(pr.getProperty("app.author"));
        assertEquals("Michael Liao", pr.getProperty("app.author", "Michael Liao"));

        // 指定类型
        assertTrue(pr.getProperty("jdbc.auto-commit", boolean.class));
        assertEquals(Boolean.TRUE, pr.getProperty("jdbc.auto-commit", Boolean.class));
        assertTrue(pr.getProperty("jdbc.detect-leak", boolean.class, true));

        int b = pr.getProperty("jdbc.pool-size", int.class);

//        assertEquals(Integer.valueOf(20), pr.getProperty("jdbc.pool-size", int.class, 999));
        int c = pr.getProperty("jdbc.idle", int.class, 5);
        System.out.println(b);
        System.out.println(c);
    }

    @Test
    public void propertyHolderOnWin() {
        String os = System.getenv("OS");
        System.out.println("env OS=" + os);

        Properties props = new Properties();
        PropertyResolver pr = new PropertyResolver(props);
        assertEquals("Windows_NT", pr.getProperty("${app.os:${OS}}"));
    }

    @Test
    public void yam2properties(){
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        Properties pros = new Properties();
        pros.putAll(config);
        PropertyResolver propertyResolver = new PropertyResolver(pros);
        System.out.println(propertyResolver.getProperty("summer.datasource.url"));
    }
}
