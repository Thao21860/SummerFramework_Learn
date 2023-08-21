package com.hello.service;

import org.summer.annotation.AutoWired;
import org.summer.annotation.Component;

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
