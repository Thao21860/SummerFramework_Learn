package org.example.scan;

import java.io.InputStream;

public class PropertiesReadTest {
    public static void main(String[] args) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties");
        System.out.println(inputStream);
        InputStream inputStream1 = ClassLoader.getSystemResourceAsStream("test.properties");
        System.out.println(inputStream1);

        System.out.println(System.getProperty("user.dir"));
    }
}
