# AI Blog

8 周实战项目：基于 Spring Boot 的 AI 博客系统。

## 技术栈

- Java 21
- Spring Boot 3.4
- MySQL
- MyBatis-Plus
- Lombok
- JWT

## 当前进度

- [x] 第 1 周：项目启动 + MySQL + 用户注册
- [x] 第 2 周：用户登录 + JWT Token

## 快速开始

### 1. 初始化数据库

在 MySQL 中执行 `sql/init.sql`（或用 Navicat / IDEA Database 运行）。

### 2. 修改数据库账号

编辑 `src/main/resources/application.yml`，改成你的 MySQL 用户名和密码：

```yaml
spring:
  datasource:
    username: root
    password: root
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 测试接口

**健康检查：**

```text
GET http://localhost:8080/hello
```

**用户注册：**

```text
POST http://localhost:8080/user/register
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "注册成功"
}
```

**用户登录：**

```text
POST http://localhost:8080/user/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 5. 验证数据库

```sql
USE ai_blog;
SELECT * FROM user;
```

## 项目结构

```text
src/main/java/com/example/xiangmu1/
├── Xiangmu1Application.java
├── controller/       # 接口层
├── service/          # 业务层
├── mapper/           # 数据库层
├── entity/           # 实体类
├── dto/              # 请求参数对象
├── util/             # 工具类（JWT 等）
└── common/           # 公共组件（异常处理等）
```
