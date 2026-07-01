-- 已有数据库执行：扩展 password 字段以存储 BCrypt 密文
USE ai_blog;
ALTER TABLE user MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密存储）';
