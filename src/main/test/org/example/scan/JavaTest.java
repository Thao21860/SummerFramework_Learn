package org.example.scan;

import java.lang.reflect.Field;
import java.util.Arrays;

public class JavaTest {
    public static void main(String[] args) throws ClassNotFoundException {
        Class clazz = Class.forName("org.example.test.jdbc.User");
        Field[] fields = clazz.getFields();
        System.out.println(Arrays.toString(fields));
    }
}
