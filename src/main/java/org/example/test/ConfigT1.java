package org.example.test;

import org.example.annotation.Bean;
import org.example.annotation.Configuration;
import org.example.annotation.Import;

@Configuration
@Import(ContextT3.class)
public class ConfigT1 {
    @Bean
    public ContextT1 getT1(){
        return new ContextT1();
    }
}
