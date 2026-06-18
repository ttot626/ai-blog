package com.example.xiangmu1.service;

import com.example.xiangmu1.vo.ArticleVO;

import java.util.List;

public interface ArticleService {

    Long add(String title, String content, Long userId);

    List<ArticleVO> list();

    ArticleVO detail(Long id);

    void update(Long id, String title, String content, Long userId);

    void delete(Long id, Long userId);
}
