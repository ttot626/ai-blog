# AI Blog

8 周实战项目：基于 Spring Boot 的 AI 博客系统，适合 Java 初学者写进简历。

## 技术栈

- Java 21
- Spring Boot 3.4
- MySQL + MyBatis-Plus
- Redis（缓存）
- JWT 鉴权
- DeepSeek API（AI 能力）
- Swagger / OpenAPI 接口文档

## 功能进度

- [x] 第 1 周：项目启动 + MySQL + 用户注册
- [x] 第 2 周：用户登录 + JWT Token
- [x] 第 3 周：文章发布 / 列表 / 详情 / 编辑 / 删除
- [x] 第 4 周：评论 / 回复 / 查询 / 删除
- [x] 第 5 周：点赞 / 收藏 / 用户主页
- [x] 第 6 周：Redis 缓存文章列表、热门文章、用户信息
- [x] 第 7 周：DeepSeek AI 摘要 / 标题优化 / 关键词 / 标签
- [x] 第 8 周：统一 Result 返回 + 全局异常 + Swagger 文档

## 快速开始

### 1. 初始化数据库

在 MySQL 中执行 `sql/init.sql`（包含 user、article、comment、article_like、favorite 表）。

如果之前已经建过库，只需补执行 `sql/week5-8.sql`。

### 2. 启动 Redis（第 6 周起需要）

```bash
# Windows 可用 Docker
docker run -d --name redis -p 6379:6379 redis:7
```

未启动 Redis 时项目仍可运行，只是缓存会自动降级为直接查数据库。

### 3. 配置 DeepSeek（第 7 周 AI 功能需要）

在 `application.yml` 中填写 API Key，或设置环境变量：

```bash
set DEEPSEEK_API_KEY=你的密钥
```

### 4. 修改数据库账号

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

### 5. 启动项目

```bash
mvn spring-boot:run
```

### 6. 查看接口文档

浏览器打开：http://localhost:8080/swagger-ui.html

## 核心接口

| 模块 | 接口 | 是否需要 Token |
|------|------|----------------|
| 健康检查 | GET /hello | 否 |
| 注册 | POST /user/register | 否 |
| 登录 | POST /user/login | 否 |
| 用户主页 | GET /user/home?userId=1 | 否（带 Token 可显示点赞/收藏状态） |
| 文章列表 | GET /article/list | 否 |
| 热门文章 | GET /article/hot?limit=10 | 否 |
| 文章详情 | GET /article/detail?id=1 | 否 |
| 发布文章 | POST /article/add | 是 |
| 点赞 | POST /article/like?articleId=1 | 是 |
| 收藏 | POST /favorite/add?articleId=1 | 是 |
| 我的收藏 | GET /favorite/list | 是 |
| AI 摘要 | POST /ai/summary | 是 |

**登录示例：**

```json
POST /user/login
{
  "username": "test",
  "password": "123456"
}
```

**统一返回格式：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**需要鉴权的请求头：**

```text
Authorization: Bearer 你的token
```

## 项目结构

```text
src/main/java/com/example/xiangmu1/
├── controller/       # 接口层
├── service/          # 业务层（含 CacheService、AiService）
├── mapper/           # 数据库层
├── entity/           # 实体类
├── dto/              # 请求参数
├── vo/               # 返回对象
├── config/           # JWT、Redis、Swagger、DeepSeek 配置
├── common/           # Result、异常处理、用户上下文
└── util/             # JWT 工具
```

## 简历描述参考

> 独立开发 AI Blog 后端项目，基于 Spring Boot 3 + MySQL + Redis，实现用户注册登录（JWT）、文章 CRUD、评论回复、点赞收藏、Redis 缓存、DeepSeek AI 辅助写作，并使用 Swagger 生成接口文档。采用 Controller-Service-Mapper 分层架构，统一 Result 响应与全局异常处理。

## 注意事项

- 当前密码为明文存储，生产环境应使用 BCrypt 加密
- 请勿将真实 API Key 和数据库密码提交到公开仓库
- GitHub 仓库：https://github.com/ttot626/ai-blog
