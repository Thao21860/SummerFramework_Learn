package org.example.service;

import org.example.annotation.AutoWired;
import org.example.annotation.Component;

import javax.annotation.PostConstruct;

@Component
public class DbInitializer {
    @AutoWired
    UserService userService;

    @PostConstruct
    void init(){
        System.out.println("初始化数据库。。。。");
        userService.initDb();
    }
}
