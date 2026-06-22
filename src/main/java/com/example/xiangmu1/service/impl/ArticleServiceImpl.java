package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.ArticleService;
import com.example.xiangmu1.service.CacheService;
import com.example.xiangmu1.service.FavoriteService;
import com.example.xiangmu1.vo.ArticleVO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleLikeService articleLikeService;
    private final FavoriteService favoriteService;
    private final CacheService cacheService;

    public ArticleServiceImpl(ArticleMapper articleMapper,
                              UserMapper userMapper,
                              ArticleLikeMapper articleLikeMapper,
                              ArticleLikeService articleLikeService,
                              FavoriteService favoriteService,
                              CacheService cacheService) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleLikeService = articleLikeService;
        this.favoriteService = favoriteService;
        this.cacheService = cacheService;
    }

    @Override
    public Long add(String title, String content, Long userId) {
        validateTitleAndContent(title, content);

        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setUserId(userId);
        article.setCreateTime(LocalDateTime.now());
        articleMapper.insert(article);
        cacheService.evictArticleList();
        cacheService.evictUserInfo(userId);
        return article.getId();
    }

    @Override
    public List<ArticleVO> list() {
        List<ArticleVO> articles = cacheService.getArticleList(this::loadArticleList, new TypeReference<>() {
        });
        enrichUserState(articles);
        return articles;
    }

    @Override
    public List<ArticleVO> hot(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        List<ArticleVO> articles = cacheService.getHotArticles(() -> loadHotArticles(safeLimit), new TypeReference<>() {
        });
        enrichUserState(articles);
        return articles;
    }

    @Override
    public ArticleVO detail(Long id) {
        Article article = getArticleOrThrow(id);
        User user = userMapper.selectById(article.getUserId());
        return toVO(article, user == null ? null : user.getUsername(), currentUserId());
    }

    @Override
    public void update(Long id, String title, String content, Long userId) {
        validateTitleAndContent(title, content);
        Article article = getArticleOrThrow(id);
        checkOwner(article, userId);

        article.setTitle(title);
        article.setContent(content);
        articleMapper.updateById(article);
        cacheService.evictArticleList();
    }

    @Override
    public void delete(Long id, Long userId) {
        Article article = getArticleOrThrow(id);
        checkOwner(article, userId);
        articleMapper.deleteById(id);
        cacheService.evictArticleList();
        cacheService.evictUserInfo(userId);
    }

    private List<ArticleVO> loadArticleList() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime)
        );
        Map<Long, String> usernameMap = loadUsernameMap(articles.stream().map(Article::getUserId).collect(Collectors.toSet()));
        return articles.stream()
                .map(article -> toVO(article, usernameMap.get(article.getUserId()), null))
                .collect(Collectors.toList());
    }

    private List<ArticleVO> loadHotArticles(int limit) {
        List<Map<String, Object>> hotRows = articleLikeMapper.selectHotArticleLikeCounts(limit);
        if (hotRows.isEmpty()) {
            return List.of();
        }

        List<Long> articleIds = hotRows.stream()
                .map(row -> ((Number) row.get("articleId")).longValue())
                .toList();
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> articleMap = articles.stream().collect(Collectors.toMap(Article::getId, a -> a));
        Map<Long, String> usernameMap = loadUsernameMap(articles.stream().map(Article::getUserId).collect(Collectors.toSet()));
        Map<Long, Long> likeCountMap = hotRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("articleId")).longValue(),
                        row -> ((Number) row.get("likeCount")).longValue()
                ));

        Long userId = currentUserId();
        List<ArticleVO> result = new ArrayList<>();
        for (Long articleId : articleIds) {
            Article article = articleMap.get(articleId);
            if (article != null) {
                ArticleVO vo = toVO(article, usernameMap.get(article.getUserId()), null);
                vo.setLikeCount(likeCountMap.getOrDefault(articleId, 0L));
                result.add(vo);
            }
        }
        return result;
    }

    private void enrichUserState(List<ArticleVO> articles) {
        Long userId = currentUserId();
        for (ArticleVO vo : articles) {
            vo.setLiked(articleLikeService.isLiked(vo.getId(), userId));
            vo.setFavorited(favoriteService.isFavorited(vo.getId(), userId));
        }
    }

    private void validateTitleAndContent(String title, String content) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("标题和内容不能为空");
        }
    }

    private Article getArticleOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        return article;
    }

    private void checkOwner(Article article, Long userId) {
        if (!article.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能操作自己的文章");
        }
    }

    private Map<Long, String> loadUsernameMap(java.util.Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? null : loginUser.getUserId();
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
        vo.setFavorited(favoriteService.isFavorited(article.getId(), currentUserId));
        return vo;
    }
}
