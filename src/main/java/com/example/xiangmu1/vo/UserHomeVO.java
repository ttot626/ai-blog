package com.example.xiangmu1.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserHomeVO {

    private Long userId;
    private String username;
    private Long articleCount;
    private Long likeCount;
    private List<ArticleVO> articles;
}
