package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.ArticleLike;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.CacheService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleMapper articleMapper;
    private final CacheService cacheService;

    public ArticleLikeServiceImpl(ArticleLikeMapper articleLikeMapper,
                                  ArticleMapper articleMapper,
                                  CacheService cacheService) {
        this.articleLikeMapper = articleLikeMapper;
        this.articleMapper = articleMapper;
        this.cacheService = cacheService;
    }

    @Override
    public void like(Long articleId, Long userId) {
        ensureArticleExists(articleId);
        if (isLiked(articleId, userId)) {
            throw new IllegalArgumentException("已经点过赞了");
        }
        ArticleLike like = new ArticleLike();
        like.setUserId(userId);
        like.setArticleId(articleId);
        like.setCreateTime(LocalDateTime.now());
        articleLikeMapper.insert(like);
        cacheService.evictArticleList();
    }

    @Override
    public void unlike(Long articleId, Long userId) {
        articleLikeMapper.delete(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getUserId, userId)
                .eq(ArticleLike::getArticleId, articleId));
        cacheService.evictArticleList();
    }

    @Override
    public long countByArticleId(Long articleId) {
        return articleLikeMapper.selectCount(
                new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId, articleId)
        );
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        return articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getUserId, userId)
                .eq(ArticleLike::getArticleId, articleId)) > 0;
    }

    private void ensureArticleExists(Long articleId) {
        if (articleMapper.selectById(articleId) == null) {
            throw new IllegalArgumentException("文章不存在");
        }
    }
}
