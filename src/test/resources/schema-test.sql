CREATE TABLE IF NOT EXISTS user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    user_id     BIGINT       NOT NULL,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    content     VARCHAR(500) NOT NULL,
    parent_id   BIGINT,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_like (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    article_id  BIGINT   NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_article UNIQUE (user_id, article_id)
);

CREATE TABLE IF NOT EXISTS favorite (
    id          BIGINT   AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    article_id  BIGINT   NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_fav_user_article UNIQUE (user_id, article_id)
);
