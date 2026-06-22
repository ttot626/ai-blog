package com.example.xiangmu1.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String username;
    private LocalDateTime createTime;
    private Long likeCount;
    private Boolean liked;
    private Boolean favorited;
}
