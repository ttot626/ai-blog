package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.Result;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.dto.CommentAddRequest;
import com.example.xiangmu1.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "评论")
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "发表评论或回复")
    @PostMapping("/add")
    public Result<Map<String, Long>> add(@RequestBody CommentAddRequest request) {
        LoginUser loginUser = UserContext.get();
        Long commentId = commentService.add(
                request.getArticleId(),
                request.getContent(),
                loginUser.getUserId(),
                request.getParentId()
        );
        return Result.success(
                request.getParentId() == null ? "评论成功" : "回复成功",
                Map.of("id", commentId)
        );
    }

    @Operation(summary = "评论列表")
    @GetMapping("/list")
    public Result<?> list(@RequestParam Long articleId) {
        return Result.success("查询成功", commentService.list(articleId));
    }

    @Operation(summary = "删除评论")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        LoginUser loginUser = UserContext.get();
        commentService.delete(id, loginUser.getUserId());
        return Result.ok("删除成功");
    }
}
