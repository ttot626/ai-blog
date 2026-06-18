package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.dto.ArticleAddRequest;
import com.example.xiangmu1.dto.ArticleUpdateRequest;
import com.example.xiangmu1.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody ArticleAddRequest request) {
        LoginUser loginUser = UserContext.get();
        Long articleId = articleService.add(request.getTitle(), request.getContent(), loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "发布成功");
        result.put("data", Map.of("id", articleId));
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", articleService.list());
        return result;
    }

    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", articleService.detail(id));
        return result;
    }

    @PostMapping("/edit")
    public Map<String, Object> edit(@RequestBody ArticleUpdateRequest request) {
        LoginUser loginUser = UserContext.get();
        articleService.update(request.getId(), request.getTitle(), request.getContent(), loginUser.getUserId());

        return Map.of(
                "code", 200,
                "message", "编辑成功"
        );
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestParam Long id) {
        LoginUser loginUser = UserContext.get();
        articleService.delete(id, loginUser.getUserId());

        return Map.of(
                "code", 200,
                "message", "删除成功"
        );
    }
}
