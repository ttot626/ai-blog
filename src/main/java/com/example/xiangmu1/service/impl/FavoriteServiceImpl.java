package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.Favorite;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.FavoriteMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.FavoriteService;
import com.example.xiangmu1.vo.ArticleVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleLikeService articleLikeService;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper,
                               ArticleMapper articleMapper,
                               UserMapper userMapper,
                               ArticleLikeService articleLikeService) {
        this.favoriteMapper = favoriteMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.articleLikeService = articleLikeService;
    }

    @Override
    public void add(Long articleId, Long userId) {
        ensureArticleExists(articleId);
        if (isFavorited(articleId, userId)) {
            throw new IllegalArgumentException("已经收藏过了");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
    }

    @Override
    public void remove(Long articleId, Long userId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId));
    }

    @Override
    public List<ArticleVO> listByUserId(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime)
        );
        if (favorites.isEmpty()) {
            return List.of();
        }
        List<Long> articleIds = favorites.stream().map(Favorite::getArticleId).toList();
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        Map<Long, String> usernameMap = loadUsernameMap(articles.stream().map(Article::getUserId).collect(Collectors.toSet()));

        List<ArticleVO> result = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Article article = articleMap.get(favorite.getArticleId());
            if (article != null) {
                result.add(toVO(article, usernameMap.get(article.getUserId()), userId));
            }
        }
        return result;
    }

    @Override
    public boolean isFavorited(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId)) > 0;
    }

    private void ensureArticleExists(Long articleId) {
        if (articleMapper.selectById(articleId) == null) {
            throw new IllegalArgumentException("文章不存在");
        }
    }

    private Map<Long, String> loadUsernameMap(java.util.Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private ArticleVO toVO(Article article, String username, Long currentUserId) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setUserId(article.getUserId());
        vo.setUsername(username);
        vo.setCreateTime(article.getCreateTime());
        vo.setLikeCount(articleLikeService.countByArticleId(article.getId()));
        vo.setLiked(articleLikeService.isLiked(article.getId(), currentUserId));
        vo.setFavorited(true);
        return vo;
    }
}
