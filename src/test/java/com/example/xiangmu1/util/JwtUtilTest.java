package com.example.xiangmu1.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-for-jwt-unit-test", 3600_000);
    }

    @Test
    void generateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "alice");

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("alice", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertNotNull(claims.getExpiration());
    }

    @Test
    void parseInvalidTokenThrows() {
        assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid.token.here"));
    }

    @Test
    void shortSecretIsHashedToValidKey() {
        JwtUtil shortKeyUtil = new JwtUtil("short", 3600_000);
        String token = shortKeyUtil.generateToken(2L, "bob");
        assertEquals(2L, shortKeyUtil.parseToken(token).get("userId", Long.class));
    }
}
