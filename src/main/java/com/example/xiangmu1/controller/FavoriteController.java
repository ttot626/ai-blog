package com.example.xiangmu1.controller;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.Result;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "收藏")
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "收藏文章")
    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long articleId) {
        LoginUser loginUser = UserContext.get();
        favoriteService.add(articleId, loginUser.getUserId());
        return Result.ok("收藏成功");
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Long articleId) {
        LoginUser loginUser = UserContext.get();
        favoriteService.remove(articleId, loginUser.getUserId());
        return Result.ok("取消收藏成功");
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/list")
    public Result<?> list() {
        LoginUser loginUser = UserContext.get();
        return Result.success("查询成功", favoriteService.listByUserId(loginUser.getUserId()));
    }
}
