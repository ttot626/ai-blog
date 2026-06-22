package com.example.xiangmu1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class CacheService {

    private static final String ARTICLE_LIST_KEY = "cache:article:list";
    private static final String HOT_ARTICLE_KEY = "cache:article:hot";
    private static final String USER_INFO_PREFIX = "cache:user:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration articleListTtl;
    private final Duration hotArticleTtl;
    private final Duration userInfoTtl;

    public CacheService(StringRedisTemplate redisTemplate,
                        ObjectMapper objectMapper,
                        @Value("${app.cache.article-list-ttl-minutes:10}") long articleListTtlMinutes,
                        @Value("${app.cache.hot-article-ttl-minutes:30}") long hotArticleTtlMinutes,
                        @Value("${app.cache.user-info-ttl-minutes:60}") long userInfoTtlMinutes) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.articleListTtl = Duration.ofMinutes(articleListTtlMinutes);
        this.hotArticleTtl = Duration.ofMinutes(hotArticleTtlMinutes);
        this.userInfoTtl = Duration.ofMinutes(userInfoTtlMinutes);
    }

    public <T> T getArticleList(Supplier<T> loader, TypeReference<T> type) {
        return getOrLoad(ARTICLE_LIST_KEY, loader, type, articleListTtl);
    }

    public <T> T getHotArticles(Supplier<T> loader, TypeReference<T> type) {
        return getOrLoad(HOT_ARTICLE_KEY, loader, type, hotArticleTtl);
    }

    public <T> T getUserInfo(Long userId, Supplier<T> loader, Class<T> type) {
        return getOrLoad(USER_INFO_PREFIX + userId, loader, type, userInfoTtl);
    }

    public void evictArticleList() {
        deleteQuietly(ARTICLE_LIST_KEY);
        deleteQuietly(HOT_ARTICLE_KEY);
    }

    public void evictUserInfo(Long userId) {
        deleteQuietly(USER_INFO_PREFIX + userId);
    }

    private <T> T getOrLoad(String key, Supplier<T> loader, Class<T> type, Duration ttl) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, type);
            }
        } catch (Exception ignored) {
        }
        T value = loader.get();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
        return value;
    }

    private <T> T getOrLoad(String key, Supplier<T> loader, TypeReference<T> type, Duration ttl) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, type);
            }
        } catch (Exception ignored) {
        }
        T value = loader.get();
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {
        }
        return value;
    }

    private void deleteQuietly(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
    }
}
