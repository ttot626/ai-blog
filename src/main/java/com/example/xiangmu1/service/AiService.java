package com.example.xiangmu1.service;

public interface AiService {

    String summary(String content);

    String optimizeTitle(String content);

    String keywords(String content);

    String tags(String content);
}
