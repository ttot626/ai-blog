package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "健康检查")
@Controller
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @Operation(summary = "健康检查")
    @GetMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello ai blog");
    }
}
