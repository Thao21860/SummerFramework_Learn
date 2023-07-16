package org.example.test;

import org.example.annotation.AutoWired;
import org.example.annotation.Component;
import org.example.annotation.Configuration;
import org.example.annotation.Import;

@Component
public class ContextT1 {
    @AutoWired
    ContextT2 contextT2;
    public void getAutoWiredTest(){
        System.out.println(contextT2);
    }
}
