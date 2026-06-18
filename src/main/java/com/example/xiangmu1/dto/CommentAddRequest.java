package com.example.xiangmu1.dto;

import lombok.Data;

@Data
public class CommentAddRequest {

    private Long articleId;
    private String content;
    private Long parentId;
}
