package org.example.scan;

import org.example.annotation.Component;
import org.example.utils.ClassUtils;

@Component(value = "111")
public class AnnotationTest {
    public static void main(String[] args) {
        Component c = ClassUtils.findAnnotation(AnnotationTest.class,Component.class);
        System.out.println(c.value());
    }
}
