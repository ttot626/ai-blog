-- Day3: 在 MySQL 中执行此脚本
CREATE DATABASE IF NOT EXISTS ai_blog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_blog;

CREATE TABLE IF NOT EXISTS user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
