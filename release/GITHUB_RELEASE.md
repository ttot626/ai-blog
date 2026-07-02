## AI Blog v{{VERSION}} — Windows 桌面版

### 下载

| 文件 | 说明 |
|------|------|
| **AIBlog-{{VERSION}}-Windows.zip** | 解压即用，内置 Java 21 运行时 |

### 安装步骤

1. 下载并解压 zip
2. 安装 MySQL，执行 `sql\init.sql`
3. 修改 `config\application.yml` 中的数据库密码
4. 双击 **`AI Blog.bat`** 启动

> 请使用 `AI Blog.bat` 启动，不要使用 `AIBlog.exe`。

### 系统要求

- Windows 10 / 11（64 位）
- MySQL 8
- Redis、DeepSeek API Key（环境变量 `DEEPSEEK_API_KEY`）为可选

### 更新日志

- Web 前端 + JavaFX 独立窗口桌面版
- 用户注册登录、文章 CRUD、评论、点赞收藏
- Redis 缓存、DeepSeek AI、Swagger 文档

完整源码：https://github.com/ttot626/ai-blog
