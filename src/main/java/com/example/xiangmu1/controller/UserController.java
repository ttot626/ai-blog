package com.example.xiangmu1.controller;

import com.example.xiangmu1.dto.LoginRequest;
import com.example.xiangmu1.dto.RegisterRequest;
import com.example.xiangmu1.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        System.out.println("注册参数 username=" + request.getUsername() + ", password=" + request.getPassword());

        userService.register(request.getUsername(), request.getPassword());

        return Map.of(
                "code", 200,
                "message", "注册成功"
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        System.out.println("登录参数 username=" + request.getUsername());

        String token = userService.login(request.getUsername(), request.getPassword());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("token", token);
        return result;
    }
}
