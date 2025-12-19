package com.financetracker.controller;

import com.financetracker.entity.UserInfo;
import com.financetracker.service.UserService;
import com.financetracker.entity.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class IntialController {

    @Autowired
    private UserService userService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the Spring Boot REST API!";
    }

//    @GetMapping("/users")
//    public List<UserInfo> getAllUsers() {
//        return userService.getAllUsers();
//    }
//
//    @GetMapping("/users/{id}")
//    public Optional<UserDto> getUserById(@PathVariable Long id) {
//        return userService.getUserById(id);
//    }
//
//    @PostMapping("/users")
//    public void addUser(@RequestBody UserInfo user) {
//        userService.addUser(user);
//    }
}
