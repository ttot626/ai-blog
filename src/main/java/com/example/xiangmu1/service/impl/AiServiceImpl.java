package com.example.xiangmu1.service.impl;

import com.example.xiangmu1.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public AiServiceImpl(RestClient deepSeekRestClient,
                         ObjectMapper objectMapper,
                         @Value("${deepseek.api-key}") String apiKey,
                         @Value("${deepseek.model}") String model) {
        this.deepSeekRestClient = deepSeekRestClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String summary(String content) {
        return chat("你是博客助手，请用中文为以下文章生成100字以内的摘要，只返回摘要正文。", content);
    }

    @Override
    public String optimizeTitle(String content) {
        return chat("你是博客助手，请根据以下文章内容生成一个更吸引人的中文标题，只返回标题，不要引号。", content);
    }

    @Override
    public String keywords(String content) {
        return chat("你是博客助手，请从以下文章中提取3到5个中文关键词，用英文逗号分隔，只返回关键词。", content);
    }

    @Override
    public String tags(String content) {
        return chat("你是博客助手，请为以下文章推荐3个中文标签，用英文逗号分隔，只返回标签。", content);
    }

    private String chat(String systemPrompt, String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (!StringUtils.hasText(apiKey) || apiKey.contains("请替换")) {
            throw new IllegalArgumentException("请先在 application.yml 或环境变量 DEEPSEEK_API_KEY 中配置 DeepSeek API Key");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", content)
                )
        );

        try {
            String responseBody = deepSeekRestClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("AI 服务调用失败：" + e.getMessage());
        }
    }
}
