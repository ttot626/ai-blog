package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleService;
import com.example.xiangmu1.vo.ArticleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper, UserMapper userMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
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
        return article.getId();
    }

    @Override
    public List<ArticleVO> list() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime)
        );
        Map<Long, String> usernameMap = loadUsernameMap(articles.stream().map(Article::getUserId).collect(Collectors.toSet()));
        return articles.stream().map(article -> toVO(article, usernameMap)).collect(Collectors.toList());
    }

    @Override
    public ArticleVO detail(Long id) {
        Article article = getArticleOrThrow(id);
        User user = userMapper.selectById(article.getUserId());
        return toVO(article, user == null ? null : user.getUsername());
    }

    @Override
    public void update(Long id, String title, String content, Long userId) {
        validateTitleAndContent(title, content);
        Article article = getArticleOrThrow(id);
        checkOwner(article, userId);

        article.setTitle(title);
        article.setContent(content);
        articleMapper.updateById(article);
    }

    @Override
    public void delete(Long id, Long userId) {
        Article article = getArticleOrThrow(id);
        checkOwner(article, userId);
        articleMapper.deleteById(id);
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

    private ArticleVO toVO(Article article, Map<Long, String> usernameMap) {
        return toVO(article, usernameMap.get(article.getUserId()));
    }

    private ArticleVO toVO(Article article, String username) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setUserId(article.getUserId());
        vo.setUsername(username);
        vo.setCreateTime(article.getCreateTime());
        return vo;
    }
}
