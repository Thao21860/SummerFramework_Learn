package com.hello.service;

import com.hello.User;
import org.summer.annotation.AutoWired;
import org.summer.annotation.Component;
import org.summer.annotation.Transactional;
import org.summer.jdbc.JdbcTemplate;

import java.util.List;
@Component
@Transactional
public class UserService {
    @AutoWired
    JdbcTemplate jdbcTemplate;

    public void initDb() {
        String sql = " CREATE TABLE IF NOT EXISTS users ( email VARCHAR(50) PRIMARY KEY, name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL)";
        jdbcTemplate.update(sql);
    }
    public User getUser(String email) {
        return jdbcTemplate.queryForObject("select * from user where email = ?", User.class, email);
    }

    public List<User> getUsers() {
        return jdbcTemplate.queryForList("select * from user", User.class);
    }

    public User createUser(String email, String name, String password) {
        User user = new User();
        user.email = email.toLowerCase();
        user.name = name;
        user.password = password;
        jdbcTemplate.update("INSERT INTO users (email, name, password) VALUES (?, ?, ?)", user.email, user.name, user.password);
        return user;
    }
}
