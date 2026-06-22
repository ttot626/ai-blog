package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "健康检查")
@RestController
public class HelloController {

    @Operation(summary = "健康检查")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello ai blog");
    }
}
