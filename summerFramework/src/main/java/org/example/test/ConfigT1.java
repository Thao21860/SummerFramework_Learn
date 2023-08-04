package org.example.test;

import org.example.annotation.*;
import org.example.test2.ContextT3;
import org.example.test2.ContextT4;

@Configuration
@Import(ContextT3.class)
public class ConfigT1 {
    // value必须给定key
    @Value("key")
    int key;
    @Bean
    public ContextT1 getT1(){
        return new ContextT1();
    }

    public int getKey() {
        return key;
    }
}
