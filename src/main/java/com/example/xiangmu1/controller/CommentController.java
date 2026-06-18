package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.dto.CommentAddRequest;
import com.example.xiangmu1.service.CommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody CommentAddRequest request) {
        LoginUser loginUser = UserContext.get();
        Long commentId = commentService.add(
                request.getArticleId(),
                request.getContent(),
                loginUser.getUserId(),
                request.getParentId()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", request.getParentId() == null ? "评论成功" : "回复成功");
        result.put("data", Map.of("id", commentId));
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long articleId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "查询成功");
        result.put("data", commentService.list(articleId));
        return result;
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestParam Long id) {
        LoginUser loginUser = UserContext.get();
        commentService.delete(id, loginUser.getUserId());

        return Map.of(
                "code", 200,
                "message", "删除成功"
        );
    }
}
