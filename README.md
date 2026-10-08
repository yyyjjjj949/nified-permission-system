# 统一权限管理系统

基于 Spring Boot、Spring Data JPA 和 RBAC0 模型实现的统一账号与权限管理系统基础版。项目按 `Controller → Service → Repository/Mapper` 分层，支持用户、角色、业务系统、菜单权限的管理，以及登录、角色分配和后端权限校验。

## 技术栈

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA / Hibernate
- H2（本地开发）和 MySQL（生产环境）
- Maven
- BCrypt 密码摘要

## 当前功能

- 用户、角色、业务系统、菜单权限的增删改查
- 用户分配和回收角色
- 角色分配和回收权限
- RBAC0 权限并集计算，多个角色的权限自动合并去重
- BCrypt 密码保存和登录接口
- Bearer Token 登录、注销和令牌校验
- 受保护示例接口的后端权限校验
- MySQL 建表脚本和生产环境配置模板

## 项目结构

```text
src/main/java/com/cdwy/permission
├── config       密码编码配置
├── controller   HTTP 接口和异常处理
├── dto          请求与响应对象
├── entity       JPA 持久化对象
├── repository   Spring Data JPA 数据访问层
└── service      RBAC0 和认证业务逻辑
docs
├── database-mysql.sql
└── rbac-api.md
```

## 本地运行

项目已配置为使用 JDK 21。PowerShell 中执行：

```powershell
$env:JAVA_HOME = 'C:\Users\姚景云\.jdks\corretto-21.0.12.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd D:\nified-permission-system
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" spring-boot:run
```

默认端口为 `8080`。本地默认使用 H2 内存数据库，应用停止后演示数据会清空。

## 接口示例

1. 创建用户：`POST /api/users`

   ```json
   {"username":"zhangsan","displayName":"张三"}
   ```

2. 设置密码：`PUT /api/users/{userId}/password`

   ```json
   {"password":"password-123"}
   ```

3. 登录：`POST /api/auth/login`

   ```json
   {"username":"zhangsan","password":"password-123"}
   ```

   登录响应中的 `accessToken` 用于请求头：

   ```text
   Authorization: Bearer <accessToken>
   ```

4. 调用受保护接口：`GET /api/demo/expenses`

   用户拥有 `expense:list` 权限时返回 `200`，权限被回收时返回 `403`，令牌注销后返回 `401`。

完整的 Postman 请求顺序和接口清单见 [docs/rbac-api.md](docs/rbac-api.md)。

## 测试和构建

```powershell
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" clean test
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" clean package
```

集成测试覆盖了 RBAC0 多角色权限合并、权限回收、密码登录和令牌注销。

## MySQL 部署

执行 [docs/database-mysql.sql](docs/database-mysql.sql) 创建生产数据库表，使用 `prod` 配置启动：

```powershell
$env:DB_URL = 'jdbc:mysql://数据库地址:3306/unified_permission?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = 'unified_permission'
$env:DB_PASSWORD = '数据库密码'
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" "-Dspring-boot.run.profiles=prod" spring-boot:run
```

生产配置位于 `src/main/resources/application-prod.yml`，密码通过环境变量提供，不写入源码。

当前令牌服务使用单机内存，重启后令牌会失效；部署到多台应用服务器时应替换为共享 Redis 或 JWT 方案。
