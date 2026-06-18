package com.example.xiangmu1.service;

import com.example.xiangmu1.vo.CommentVO;

import java.util.List;

public interface CommentService {

    Long add(Long articleId, String content, Long userId, Long parentId);

    List<CommentVO> list(Long articleId);

    void delete(Long id, Long userId);
}
