package com.learn.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id) {
        return "User " + id;
    }

    @GetMapping("/user/list")
    public String listUsers() {
        return "User List: [1, 2, 3]";
    }
}
