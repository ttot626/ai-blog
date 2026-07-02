package com.example.xiangmu1.service.impl;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.vo.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.CacheService;
import com.example.xiangmu1.service.FavoriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ArticleLikeMapper articleLikeMapper;
    @Mock
    private ArticleLikeService articleLikeService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private CacheService cacheService;

    private ArticleServiceImpl articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleServiceImpl(
                articleMapper, userMapper, articleLikeMapper,
                articleLikeService, favoriteService, cacheService
        );
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void listUsesPaginationAndCache() {
        when(cacheService.getArticleListPage(anyInt(), anyInt(), any(), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> loader = invocation.getArgument(2);
                    return loader.get();
                });
        when(articleMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Article> page = invocation.getArgument(0);
            Article article = new Article();
            article.setId(1L);
            article.setTitle("标题");
            article.setContent("内容");
            article.setUserId(1L);
            page.setRecords(java.util.List.of(article));
            page.setTotal(1);
            page.setCurrent(1);
            page.setSize(10);
            page.setPages(1);
            return page;
        });
        when(userMapper.selectBatchIds(any())).thenReturn(java.util.List.of());

        PageResult<?> result = articleService.list(1, 10);

        org.junit.jupiter.api.Assertions.assertEquals(1, result.getTotal());
        org.junit.jupiter.api.Assertions.assertEquals(1, result.getRecords().size());
        verify(cacheService).getArticleListPage(eq(1), eq(10), any(), any());
    }

    @Test
    void listClampsPageSize() {
        when(cacheService.getArticleListPage(anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageResult<>(java.util.List.of(), 0, 1, 50, 0));

        articleService.list(0, 999);

        verify(cacheService).getArticleListPage(eq(1), eq(50), any(), any());
    }

    @Test
    void addArticleSuccess() {
        when(articleMapper.insert(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(100L);
            return 1;
        });

        Long id = articleService.add("标题", "正文内容", 1L);

        assertEquals(100L, id);
        verify(cacheService).evictArticleList();
        verify(cacheService).evictUserInfo(1L);
    }

    @Test
    void addArticleEmptyTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> articleService.add("", "正文", 1L));
    }

    @Test
    void deleteOnlyOwnerAllowed() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(2L);
        when(articleMapper.selectById(1L)).thenReturn(article);

        assertThrows(IllegalArgumentException.class,
                () -> articleService.delete(1L, 1L));
    }

    @Test
    void deleteSuccess() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);
        when(articleMapper.selectById(1L)).thenReturn(article);

        articleService.delete(1L, 1L);

        verify(articleMapper).deleteById(1L);
        verify(cacheService).evictArticleList();
    }

    @Test
    void hotLimitIsClamped() {
        when(articleLikeMapper.selectHotArticleLikeCounts(20)).thenReturn(java.util.List.of());
        when(cacheService.getHotArticles(any(), any())).thenAnswer(invocation -> invocation.getArgument(0, java.util.function.Supplier.class).get());

        articleService.hot(999);

        verify(articleLikeMapper).selectHotArticleLikeCounts(20);
    }

    @Test
    void updateRejectsNonOwner() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(99L);
        when(articleMapper.selectById(1L)).thenReturn(article);

        assertThrows(IllegalArgumentException.class,
                () -> articleService.update(1L, "新标题", "新内容", 1L));
    }
}
