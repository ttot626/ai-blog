package com.example.xiangmu1.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private RestClient deepSeekRestClient;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(deepSeekRestClient, new ObjectMapper(), "", "deepseek-chat");
    }

    @Test
    void summaryRequiresApiKey() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> aiService.summary("文章内容"));
        assertEquals("请配置 DeepSeek API Key（环境变量 DEEPSEEK_API_KEY 或 .env 文件）", ex.getMessage());
    }

    @Test
    void summaryRequiresContent() {
        aiService = new AiServiceImpl(deepSeekRestClient, new ObjectMapper(), "sk-test", "deepseek-chat");

        assertThrows(IllegalArgumentException.class, () -> aiService.summary("  "));
    }
}
