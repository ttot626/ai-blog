# AI Blog

基于 Spring Boot 的全栈 AI 博客系统，支持 **Web 网页** 与 **JavaFX 桌面独立窗口** 两种使用方式。

[![GitHub](https://img.shields.io/badge/GitHub-ttot626%2Fai--blog-blue)](https://github.com/ttot626/ai-blog)

## 功能概览

| 模块 | 能力 |
|------|------|
| 用户 | 注册、登录（JWT）、个人主页 |
| 文章 | 发布、编辑、删除、列表、详情、热门排行 |
| 互动 | 评论与回复、点赞、收藏 |
| 缓存 | Redis 缓存文章列表、热门文章、用户信息 |
| AI | DeepSeek 摘要、标题优化、关键词与标签生成 |
| 文档 | Swagger / OpenAPI 接口文档 |

## 技术栈

Java 21 · Spring Boot 3.4 · MySQL · MyBatis-Plus · Redis · JWT · JavaFX · DeepSeek · Swagger

## 快速开始

### 1. 初始化数据库

在 MySQL 中执行 [`sql/init.sql`](sql/init.sql)，将创建 `ai_blog` 库及全部数据表。

### 2. 修改配置

编辑 [`src/main/resources/application.yml`](src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

DeepSeek AI 功能需配置 API Key（环境变量 `DEEPSEEK_API_KEY` 或配置文件）。

Redis 未启动时项目仍可运行，缓存会自动降级为直接查库。

### 3. 开发运行

```bash
mvn spring-boot:run
```

| 模式 | 入口类 | 说明 |
|------|--------|------|
| 网页版 | `Xiangmu1Application` | 浏览器访问 http://localhost:8080 |
| 桌面版 | `DesktopLauncher` | JavaFX 独立窗口，内嵌 WebView |

接口文档：http://localhost:8080/swagger-ui.html

### 4. 打包桌面软件

1. 安装 JDK 21（含 `jpackage`）
2. 暂停 OneDrive 同步，关闭占用 8080 端口的进程
3. 双击 [`打包EXE.bat`](打包EXE.bat)
4. 启动：`C:\AIBlog-build\AIBlog\AI Blog.bat`

> 请使用 `AI Blog.bat` 或 `AI Blog.vbs` 启动，勿用 `AIBlog.exe`（部分环境会报 JVM 启动失败）。若内置 Java 缺失，运行目录下的 `修复启动.bat`。

## 核心接口

| 接口 | 方法 | 鉴权 |
|------|------|------|
| `/user/register` | POST | 否 |
| `/user/login` | POST | 否 |
| `/article/list` | GET | 否 |
| `/article/hot` | GET | 否 |
| `/article/add` | POST | 是 |
| `/article/like` | POST | 是 |
| `/favorite/add` | POST | 是 |
| `/ai/summary` | POST | 是 |

鉴权请求头：`Authorization: Bearer <token>`

统一响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 项目结构

```text
├── src/main/java/          # 后端业务代码
├── src/main/resources/
│   ├── static/             # Web 前端（HTML / CSS / JS）
│   └── application.yml
├── sql/init.sql            # 数据库初始化脚本
├── installer/              # 桌面版配置与修复脚本
└── 打包EXE.bat              # 一键打包桌面版
```

## 注意事项

- 当前密码为明文存储，生产环境请使用 BCrypt 等加密方案
- 请勿将真实 API Key 和数据库密码提交到公开仓库
- 打包输出目录为 `C:\AIBlog-build\`，不在仓库内

## 仓库

https://github.com/ttot626/ai-blog
