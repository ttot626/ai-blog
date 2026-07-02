package com.example.xiangmu1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xiangmu1.entity.User;
import com.example.xiangmu1.mapper.ArticleLikeMapper;
import com.example.xiangmu1.mapper.ArticleMapper;
import com.example.xiangmu1.mapper.UserMapper;
import com.example.xiangmu1.service.ArticleLikeService;
import com.example.xiangmu1.service.CacheService;
import com.example.xiangmu1.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private ArticleLikeMapper articleLikeMapper;
    @Mock
    private ArticleLikeService articleLikeService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CacheService cacheService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userMapper, articleMapper, articleLikeMapper,
                articleLikeService, jwtUtil, cacheService, passwordEncoder
        );
    }

    @Test
    void registerSuccess() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        userService.register("alice", "password123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("alice", saved.getUsername());
        assertTrue(passwordEncoder.matches("password123", saved.getPassword()));
    }

    @Test
    void registerDuplicateUsername() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "password123"));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void registerInvalidUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("a", "password123"));
    }

    @Test
    void registerShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "12345"));
    }

    @Test
    void loginSuccessWithBcryptPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(passwordEncoder.encode("password123"));

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtil.generateToken(1L, "alice")).thenReturn("token-abc");

        String token = userService.login("alice", "password123");

        assertEquals("token-abc", token);
    }

    @Test
    void loginUpgradesLegacyPlainPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("password123");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtil.generateToken(1L, "alice")).thenReturn("token-abc");

        userService.login("alice", "password123");

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void loginWrongPassword() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword(passwordEncoder.encode("password123"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("alice", "wrong-pass"));
    }

    @Test
    void loginUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login("ghost", "password123"));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void getUserIdByUsername() {
        User user = new User();
        user.setId(9L);
        user.setUsername("alice");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        assertEquals(9L, userService.getUserIdByUsername("alice"));
    }
}
