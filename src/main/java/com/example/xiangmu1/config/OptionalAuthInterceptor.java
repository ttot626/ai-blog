package com.example.xiangmu1.config;

import com.example.xiangmu1.common.LoginUser;
import com.example.xiangmu1.common.UserContext;
import com.example.xiangmu1.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选登录：有 Token 则解析用户信息，没有 Token 也放行。
 */
@Component
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public OptionalAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String token = authorization.substring(7);
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                UserContext.set(new LoginUser(userId, username));
            } catch (JwtException ignored) {
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
