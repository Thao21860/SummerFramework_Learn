package org.example.test;

import org.example.annotation.Component;
import org.example.annotation.Value;

@Component
public class ContextT2 {
    @Value("${contextT2.key}")
    public String key;
    @Value("${contextT2.value}")
    public String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
