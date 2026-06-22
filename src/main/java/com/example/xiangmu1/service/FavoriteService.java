package com.example.xiangmu1.service;

import com.example.xiangmu1.vo.ArticleVO;

import java.util.List;

public interface FavoriteService {

    void add(Long articleId, Long userId);

    void remove(Long articleId, Long userId);

    List<ArticleVO> listByUserId(Long userId);

    boolean isFavorited(Long articleId, Long userId);
}
