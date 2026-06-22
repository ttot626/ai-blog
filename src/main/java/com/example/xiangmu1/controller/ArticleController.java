package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.Result;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.dto.ArticleAddRequest;
import com.example.xiangmu1.dto.ArticleUpdateRequest;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "文章")
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleLikeService articleLikeService;

    public ArticleController(ArticleService articleService, ArticleLikeService articleLikeService) {
        this.articleService = articleService;
        this.articleLikeService = articleLikeService;
    }

    @Operation(summary = "发布文章")
    @PostMapping("/add")
    public Result<Map<String, Long>> add(@RequestBody ArticleAddRequest request) {
        LoginUser loginUser = UserContext.get();
        Long articleId = articleService.add(request.getTitle(), request.getContent(), loginUser.getUserId());
        return Result.success("发布成功", Map.of("id", articleId));
    }

    @Operation(summary = "文章列表")
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success("查询成功", articleService.list());
    }

    @Operation(summary = "热门文章")
    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success("查询成功", articleService.hot(limit));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/detail")
    public Result<?> detail(@RequestParam Long id) {
        return Result.success("查询成功", articleService.detail(id));
    }

    @Operation(summary = "编辑文章")
    @PostMapping("/edit")
    public Result<Void> edit(@RequestBody ArticleUpdateRequest request) {
        LoginUser loginUser = UserContext.get();
        articleService.update(request.getId(), request.getTitle(), request.getContent(), loginUser.getUserId());
        return Result.ok("编辑成功");
    }

    @Operation(summary = "删除文章")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        LoginUser loginUser = UserContext.get();
        articleService.delete(id, loginUser.getUserId());
        return Result.ok("删除成功");
    }

    @Operation(summary = "点赞文章")
    @PostMapping("/like")
    public Result<Void> like(@RequestParam Long articleId) {
        LoginUser loginUser = UserContext.get();
        articleLikeService.like(articleId, loginUser.getUserId());
        return Result.ok("点赞成功");
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public Result<Void> unlike(@RequestParam Long articleId) {
        LoginUser loginUser = UserContext.get();
        articleLikeService.unlike(articleId, loginUser.getUserId());
        return Result.ok("取消点赞成功");
    }
}
