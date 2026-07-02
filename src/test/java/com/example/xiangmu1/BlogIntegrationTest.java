package com.example.xiangmu1;

import com.example.xiangmu1.support.TestRedisConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@Transactional
class BlogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginPublishAndListArticle() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("data").path("token").asText();

        mockMvc.perform(post("/article/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"第一篇","content":"集成测试正文"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());

        mockMvc.perform(get("/article/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("第一篇"));
    }

    @Test
    void protectedEndpointRequiresLogin() throws Exception {
        mockMvc.perform(post("/article/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"x","content":"y"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void likeArticleFlow() throws Exception {
        registerAndLogin("liker", "password123");
        String token = loginToken("liker", "password123");

        MvcResult addResult = mockMvc.perform(post("/article/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"可点赞","content":"内容"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long articleId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/article/like")
                        .header("Authorization", "Bearer " + token)
                        .param("articleId", String.valueOf(articleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/article/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode first = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data").get(0);
        assertEquals(articleId, first.path("id").asLong());
        assertTrue(first.path("liked").asBoolean());
        assertEquals(1, first.path("likeCount").asInt());
    }

    @Test
    void duplicateLikeIsRejected() throws Exception {
        registerAndLogin("dupuser", "password123");
        String token = loginToken("dupuser", "password123");

        MvcResult addResult = mockMvc.perform(post("/article/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"重复点赞","content":"内容"}
                                """))
                .andReturn();
        long articleId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/article/like")
                        .header("Authorization", "Bearer " + token)
                        .param("articleId", String.valueOf(articleId)))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/article/like")
                        .header("Authorization", "Bearer " + token)
                        .param("articleId", String.valueOf(articleId)))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("已经点过赞了"));
    }

    @Test
    void helloEndpointWorks() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private void registerAndLogin(String username, String password) throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(jsonPath("$.code").value(200));
    }

    private String loginToken(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }
}
