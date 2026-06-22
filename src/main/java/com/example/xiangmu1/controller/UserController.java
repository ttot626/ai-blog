package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.Result;
import com.example.xiangmu1.dto.LoginRequest;
import com.example.xiangmu1.dto.RegisterRequest;
import com.example.xiangmu1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "用户")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return Result.ok("注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return Result.success("登录成功", Map.of("token", token));
    }

    @Operation(summary = "用户主页")
    @GetMapping("/home")
    public Result<?> home(@RequestParam Long userId) {
        return Result.success("查询成功", userService.home(userId));
    }
}
