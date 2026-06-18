package com.example.xiangmu1.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of(
                "code", 400,
                "message", e.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Map<String, Object> handleUnauthorized(UnauthorizedException e) {
        return Map.of(
                "code", 401,
                "message", e.getMessage()
        );
    }
}
