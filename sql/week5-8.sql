-- 第5-8周：点赞、收藏表
USE ai_blog;

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
