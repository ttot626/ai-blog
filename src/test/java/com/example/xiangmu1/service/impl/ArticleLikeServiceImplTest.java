package com.example.xiangmu1.service.impl;

import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.ArticleLike;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleLikeServiceImplTest {

    @Mock
    private ArticleLikeMapper articleLikeMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private CacheService cacheService;

    private ArticleLikeServiceImpl articleLikeService;

    @BeforeEach
    void setUp() {
        articleLikeService = new ArticleLikeServiceImpl(articleLikeMapper, articleMapper, cacheService);
    }

    @Test
    void likeArticleNotFound() {
        when(articleMapper.selectById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> articleLikeService.like(1L, 1L));
        verify(articleLikeMapper, never()).insert(any(ArticleLike.class));
    }

    @Test
    void likeDuplicateRejected() {
        Article article = new Article();
        article.setId(1L);
        when(articleMapper.selectById(1L)).thenReturn(article);
        when(articleLikeMapper.selectCount(any())).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> articleLikeService.like(1L, 1L));
    }

    @Test
    void isLikedReturnsFalseForAnonymousUser() {
        boolean liked = articleLikeService.isLiked(1L, null);
        org.junit.jupiter.api.Assertions.assertFalse(liked);
        verify(articleLikeMapper, never()).selectCount(any());
    }
}
