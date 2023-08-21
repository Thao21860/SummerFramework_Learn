package com.hello.webapp;

import com.hello.User;
import org.summer.annotation.AutoWired;
import org.summer.annotation.GetMapping;
import org.summer.annotation.PathVariable;
import org.summer.annotation.RestController;
import org.summer.exception.DataAccessException;
import com.hello.service.UserService;

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
