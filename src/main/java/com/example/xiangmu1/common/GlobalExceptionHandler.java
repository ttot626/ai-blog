package com.example.xiangmu1.common;

import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.fail(401, e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccess(DataAccessException e) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.contains("doesn't exist")) {
            return Result.fail(500, "数据库表缺失，请执行 sql/init.sql");
        }
        return Result.fail(500, "数据库操作失败");
    }
}
