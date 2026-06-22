package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.Article;
import com.example.xiangmu1.entity.ArticleLike;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.CacheService;
import com.example.xiangmu1.service.UserService;
import com.example.xiangmu1.util.JwtUtil;
import com.example.xiangmu1.vo.ArticleVO;
import com.example.xiangmu1.vo.UserHomeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleLikeService articleLikeService;
    private final JwtUtil jwtUtil;
    private final CacheService cacheService;

    public UserServiceImpl(UserMapper userMapper,
                           ArticleMapper articleMapper,
                           ArticleLikeMapper articleLikeMapper,
                           ArticleLikeService articleLikeService,
                           JwtUtil jwtUtil,
                           CacheService cacheService) {
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleLikeService = articleLikeService;
        this.jwtUtil = jwtUtil;
        this.cacheService = cacheService;
    }

    @Override
    public void register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!password.equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public UserHomeVO home(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return cacheService.getUserInfo(userId, () -> loadUserHome(userId), UserHomeVO.class);
    }

    private UserHomeVO loadUserHome(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getUserId, userId)
                        .orderByDesc(Article::getCreateTime)
        );

        long totalLikes = 0;
        if (!articles.isEmpty()) {
            List<Long> articleIds = articles.stream().map(Article::getId).toList();
            totalLikes = articleLikeMapper.selectCount(
                    new LambdaQueryWrapper<ArticleLike>().in(ArticleLike::getArticleId, articleIds)
            );
        }

        List<ArticleVO> articleVOList = articles.stream().map(article -> {
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setContent(article.getContent());
            vo.setUserId(article.getUserId());
            vo.setUsername(user.getUsername());
            vo.setCreateTime(article.getCreateTime());
            vo.setLikeCount(articleLikeService.countByArticleId(article.getId()));
            vo.setLiked(false);
            vo.setFavorited(false);
            return vo;
        }).toList();

        UserHomeVO homeVO = new UserHomeVO();
        homeVO.setUserId(user.getId());
        homeVO.setUsername(user.getUsername());
        homeVO.setArticleCount((long) articles.size());
        homeVO.setLikeCount(totalLikes);
        homeVO.setArticles(articleVOList);
        return homeVO;
    }
}
