package com.example.xiangmu1.dto;

import lombok.Data;

@Data
public class ArticleUpdateRequest {

    private Long id;
    private String title;
    private String content;
}
