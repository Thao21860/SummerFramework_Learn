package org.example.web;

import org.example.User;
import org.example.annotation.AutoWired;
import org.example.annotation.GetMapping;
import org.example.annotation.PathVariable;
import org.example.annotation.RestController;
import org.example.exception.DataAccessException;
import org.example.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    @AutoWired
    UserService userService;

    @GetMapping("/api/user/{email}")
    Boolean userExist(@PathVariable("email") String email) {
        Map<String, Boolean> res = null;
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        try {
            userService.getUser(email);
            return res.put("result", Boolean.TRUE);
        } catch (DataAccessException e) {
            return res.put("result", Boolean.FALSE);
        }
    }
    @GetMapping("/api/users")
    List<User> users(){return userService.getUsers();}
}
