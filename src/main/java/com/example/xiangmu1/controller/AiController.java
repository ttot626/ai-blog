package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.Result;
import com.example.xiangmu1.dto.AiContentRequest;
import com.example.xiangmu1.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AI 能力")
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "生成文章摘要")
    @PostMapping("/summary")
    public Result<Map<String, String>> summary(@RequestBody AiContentRequest request) {
        return Result.success("生成成功", Map.of("result", aiService.summary(request.getContent())));
    }

    @Operation(summary = "优化标题")
    @PostMapping("/title")
    public Result<Map<String, String>> title(@RequestBody AiContentRequest request) {
        return Result.success("生成成功", Map.of("result", aiService.optimizeTitle(request.getContent())));
    }

    @Operation(summary = "提取关键词")
    @PostMapping("/keywords")
    public Result<Map<String, String>> keywords(@RequestBody AiContentRequest request) {
        return Result.success("生成成功", Map.of("result", aiService.keywords(request.getContent())));
    }

    @Operation(summary = "推荐标签")
    @PostMapping("/tags")
    public Result<Map<String, String>> tags(@RequestBody AiContentRequest request) {
        return Result.success("生成成功", Map.of("result", aiService.tags(request.getContent())));
    }
}
