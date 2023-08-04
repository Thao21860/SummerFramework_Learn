package org.example.test;

import org.example.annotation.Around;
import org.example.annotation.Component;
import org.example.annotation.Value;

@Component
@Around("politeInvocationHandler")
public class ContextT2 {
    @Value("${contextT2.key:1}")
    public String key;
    @Value("${contextT2.value:1}")
    public String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}