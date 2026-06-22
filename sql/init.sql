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

CREATE TABLE IF NOT EXISTS article (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    title       VARCHAR(200)  NOT NULL COMMENT '标题',
    content     TEXT          NOT NULL COMMENT '正文',
    user_id     BIGINT        NOT NULL COMMENT '作者ID',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    article_id  BIGINT        NOT NULL COMMENT '文章ID',
    user_id     BIGINT        NOT NULL COMMENT '评论用户ID',
    content     VARCHAR(500)  NOT NULL COMMENT '评论内容',
    parent_id   BIGINT        NULL COMMENT '父评论ID，为空表示顶级评论',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (id),
    KEY idx_article_id (article_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE IF NOT EXISTS article_like (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    article_id  BIGINT   NOT NULL COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

CREATE TABLE IF NOT EXISTS favorite (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    article_id  BIGINT   NOT NULL COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_article (user_id, article_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
