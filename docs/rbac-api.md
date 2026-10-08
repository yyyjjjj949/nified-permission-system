# RBAC0 基础接口

当前项目使用 H2 内存数据库启动，应用停止后数据会清空。接口没有引入外部工程，所有代码都在本项目中。

启动：

```powershell
$env:JAVA_HOME = 'C:\Users\姚景云\.jdks\corretto-21.0.12.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd D:\nified-permission-system
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" spring-boot:run
```

请求均使用 `Content-Type: application/json`。

## 建立 RBAC0 演示数据

1. 创建业务系统：`POST /api/systems`

```json
{"code":"finance","name":"财务系统","baseUrl":"http://finance.local"}
```

2. 创建用户：`POST /api/users`

```json
{"username":"zhangsan","displayName":"张三"}
```

3. 设置密码：`PUT /api/users/{userId}/password`

```json
{"password":"password-123"}
```

4. 创建角色：`POST /api/roles`

```json
{"code":"finance_viewer","name":"财务查看员","description":"查看报销"}
```

5. 创建菜单权限：`POST /api/permissions`

```json
{"systemId":1,"code":"expense:list","name":"查看报销","menuPath":"/expense/list"}
```

6. 给角色分配权限：`PUT /api/roles/{roleId}/permissions/{permissionId}`。该请求返回 `204`。

7. 给用户分配角色：`PUT /api/users/{userId}/roles/{roleId}`。该请求返回 `204`。

8. 查询用户最终权限：`GET /api/users/{userId}/permissions`。多个角色的权限会合并并去重。

9. 登录：`POST /api/auth/login`

```json
{"username":"zhangsan","password":"password-123"}
```

响应中的 `accessToken` 用于后续接口的 `Authorization: Bearer <accessToken>` 请求头。密码只保存 BCrypt 摘要，登录响应不会返回密码。

10. 调用受保护示例接口：`GET /api/demo/expenses`，并添加请求头 `Authorization: Bearer <accessToken>`。拥有 `expense:list` 时返回 `200`，移除角色后返回 `403`。

11. 注销：`POST /api/auth/logout`，添加同一个 `Authorization` 请求头；注销后令牌返回 `401`。

## 其他接口

| 方法 | 路径 | 作用 |
|---|---|---|
| GET | `/api/users` | 查询用户 |
| PUT | `/api/users/{userId}` | 修改用户名称和启用状态 |
| DELETE | `/api/users/{userId}` | 删除用户及其角色关系 |
| GET | `/api/roles` | 查询角色 |
| PUT | `/api/roles/{roleId}` | 修改角色名称、描述和启用状态 |
| DELETE | `/api/roles/{roleId}` | 删除角色及其关联关系 |
| GET | `/api/systems` | 查询业务系统 |
| PUT | `/api/systems/{systemId}` | 修改业务系统 |
| DELETE | `/api/systems/{systemId}` | 删除系统及其权限 |
| GET | `/api/permissions?systemId={id}` | 查询菜单权限 |
| PUT | `/api/permissions/{permissionId}` | 修改权限名称和菜单路径 |
| DELETE | `/api/permissions/{permissionId}` | 删除权限及其角色关联 |
| GET | `/api/users/{userId}/roles` | 查询用户角色 |
| GET | `/api/roles/{roleId}/permissions` | 查询角色权限 |
| GET | `/api/users/{userId}/permissions/check?code=expense:list` | 管理端检查单项权限 |
| DELETE | `/api/users/{userId}/roles/{roleId}` | 回收用户角色 |
| DELETE | `/api/roles/{roleId}/permissions/{permissionId}` | 回收角色权限 |

生产环境可以执行 `database-mysql.sql` 建表，再通过 `application-prod.yml` 配置 MySQL；当前 `application.properties` 的 H2 配置用于本地开发和演示。
