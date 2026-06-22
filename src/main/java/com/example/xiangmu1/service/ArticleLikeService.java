package com.example.xiangmu1.service;

public interface ArticleLikeService {

    void like(Long articleId, Long userId);

    void unlike(Long articleId, Long userId);

    long countByArticleId(Long articleId);

    boolean isLiked(Long articleId, Long userId);
}
