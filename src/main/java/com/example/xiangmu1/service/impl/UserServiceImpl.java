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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           ArticleMapper articleMapper,
                           ArticleLikeMapper articleLikeMapper,
                           ArticleLikeService articleLikeService,
                           JwtUtil jwtUtil,
                           CacheService cacheService,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleLikeService = articleLikeService;
        this.jwtUtil = jwtUtil;
        this.cacheService = cacheService;
        this.passwordEncoder = passwordEncoder;
    }

    private void validateUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度为 3～20 个字符");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字和下划线");
        }
    }

    private boolean isBcryptHash(String stored) {
        return stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String stored = user.getPassword();
        if (isBcryptHash(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        if (!rawPassword.equals(stored)) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.updateById(user);
        return true;
    }

    @Override
    public void register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        validateUsername(username.trim());
        username = username.trim();
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        if (password.length() > 50) {
            throw new IllegalArgumentException("密码长度不能超过50位");
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
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

        if (!matchesPassword(password, user)) {
            throw new IllegalArgumentException("密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public Long getUserIdByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user.getId();
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
