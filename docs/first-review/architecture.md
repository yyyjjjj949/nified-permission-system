# 代码架构与实现说明

## 结构

这是一个Spring Boot后端应用，配套Vue前端；不是多个独立微服务。后端主要调用链是Controller → Service → Repository → 数据库，Entity表达持久化模型，DTO表达请求/响应，config负责横切配置。

```text
com.cdwy.permission
├── controller/{auth,user,organization,role,system,permission,resource,rbac,me,demo,common}
├── service/{auth,user,organization,role,system,permission,resource,rbac}
├── repository/{user,organization,role,system,permission,resource,rbac}
├── entity/{user,organization,role,system,permission,resource,rbac}
├── dto/{auth,user,organization,role,system,permission,resource,common}
├── config
└── exception
```

“先分层后分模块”表示先划分controller/service等技术层，每层再按领域分包。领域职责为认证、用户、组织、角色、应用、前端权限、API资源、授权8组；个人API与课程示例是交互入口，不另造一套人员/权限数据。

## 10个Spring Service

| Service | 职责 |
|---|---|
| AdminBootstrapService | 启动时创建首个管理员；无HTTP初始化入口，不覆盖既有管理员密码，不把普通用户提权 |
| AuthService | 登录、解析当前账号、本人改密 |
| JwtTokenService | 签发和验证JWT，自身不访问数据库，不保存登录记录 |
| UserService | 用户资料、身份期限、账号状态、密码与删除 |
| DepartmentService | 部门树、重名、防环、删除引用检查 |
| RoleService | 业务角色资料、独立状态、删除引用检查 |
| SystemService | 应用登记、独立状态、删除引用检查 |
| PermissionService | 菜单按钮树、状态、API绑定和树输出 |
| ApiResourceService | 独立后端接口资源登记、状态与删除 |
| AuthorizationService | 用户角色、角色权限、有效权限并集及实际API请求授权 |

service目录中的PasswordPolicy和UserViews是静态辅助类，不是额外的Spring Service。third-party/clonefactory是仓库附带的注解处理器依赖，有独立POM，不是运行时业务服务。

## 身份认证与业务授权

SecurityConfig关闭表单登录、HTTP Basic、Cookie登录与服务端Session，使用STATELESS。实际API身份边界由ApiAccessConfig注册的HandlerInterceptor执行；`permitAll`是过滤链让请求进入MVC，不表示管理接口无鉴权。登录以外的/api/**在Controller前解析Bearer Token。

1. JwtTokenService验签并检查sub、iat、exp、cv。
2. AuthService查询当前用户，检查enabled、expiresAt与账号级credentialVersion。
3. /api/me及其子路径只使用当前身份；管理接口需要administrator。
4. 带@BusinessAccess的业务控制器按注解应用编码、实际方法、请求路径调用AuthorizationService.requireApi。
5. 先选中匹配的最具体API资源，再检查其状态；最具体资源停用时不能退回更宽泛的授权路径。计算用户有效角色的前端权限并集，经PermissionApi判断是否允许；缺任意环节拒绝。

Token里没有角色/权限缓存。授权变更由下一次请求读取；JWT无服务端登录会话，不等于业务处理完全不查询数据库。管理员身份不自动获得业务接口访问权。

## 状态、期限与退出

资料更新DTO不含enabled/active。独立状态API改变对象可用性；父菜单或所属应用停用使后代权限失效，关联保留。用户停用/重新启用、密码变更会递增credentialVersion，旧Token即使尚未到期也无效。

前端Token保存在本标签页sessionStorage，刷新可恢复；退出删除本地Token，没有单个令牌立即撤销接口。登录会话表、刷新令牌及撤销黑名单均不在本轮范围。

UserAccount用personType与expiresAt表达正式、外部、临时身份。后两者接收expiresOn日期，按配置时区换算成次日零点排他上界。默认业务时区Asia/Shanghai，前端日期编辑按此演示时区展示；如切换业务时区，需同步调整前端日期显示。期限上限从配置读取，不依赖前端限制。

## 持久化和事务

Repository继承JpaRepository，实体关系用@ManyToOne映射，三张有组合唯一键的关联实体表达M:N。Controller没有JdbcTemplate或SQL。仅测试使用JdbcTemplate准备隔离数据、导出实际结构。

Service默认只读事务，写操作显式@Transactional。用户敏感写使用PESSIMISTIC_WRITE锁，JPA脏检查更新受管实体。数据库约束兜底并发重复与引用冲突；API异常处理器映射为400/401/403/404/409。

UserAccount的@SecondaryTable把登录名、密码摘要放在sys_account，其余资料放sys_user。它是一个JPA用户实体的物理拆分，不是人员与账号两套业务对象。

## CloneFactory

[CreateUserRequest](../../src/main/java/com/cdwy/permission/dto/user/CreateUserRequest.java)使用@CopyTo(UserAccount.class)，[UserView](../../src/main/java/com/cdwy/permission/dto/user/UserView.java)使用@CopyFrom(UserAccount.class)。Maven编译时生成字段拷贝代码。

创建用户时复制允许输入的同名资料，Service单独处理BCrypt、部门和有效期；普通输入没有administrator和credentialVersion。输出UserView只声明允许展示的字段，不会输出passwordHash。其他领域使用record DTO显式映射，无需为每个对象强行增加CloneFactory调用。

生成代码位于target/generated-sources/annotations，不手工编辑。首次干净环境先构建third-party/clonefactory；[构建脚本](../../scripts/build-first-review.ps1)已包含这一步。

## 当前边界

本轮为单实例教学演示，未实现角色继承、互斥、数据行级权限、委派管理员、生产负载优化、并发首次管理员初始化互斥和真实外部系统联调。AuthorizationService当前逐角色读取关系，规模增长时需要批量查询与可验证的缓存失效策略。默认H2内存重启清空，MySQL使用新库脚本及validate模式，旧库迁移单独实施。
