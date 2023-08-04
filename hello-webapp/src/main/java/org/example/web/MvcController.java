package org.example.web;

import org.example.User;
import org.example.annotation.AutoWired;
import org.example.annotation.Controller;
import org.example.annotation.GetMapping;
import org.example.mvc.web.ModelAndView;
import org.example.service.UserService;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MvcController {
    @AutoWired
    UserService userService;

    static final String USER_SESSION_KEY = "__user__";


    @GetMapping("/")
    ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            return new ModelAndView("redirect:/register");
        }
        Map<String, Object> res = null;
        res.put("user", user);
        return new ModelAndView("/index.html", res);
    }

    @GetMapping("/api/users")
    List<User> users() {
        return userService.getUsers();
    }

}
