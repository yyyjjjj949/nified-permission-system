# 第一次检查整改说明

项目仓库：[yyyjjjj949/nified-permission-system](https://github.com/yyyjjjj949/nified-permission-system)，当前交付位于 `main`。整改依据为第一次检查意见，原参考基线为 Gitee `d2f243819c74b069f869ecd53a82cdbb6b2eca2d`。

本轮依据老师第一次检查意见收敛为可解释、可验证的基础RBAC系统，并按补充要求使用JWT Token登录。原版本材料归档于[archive/20261028](../archive/20261028/README.md)，GitHub既有提交历史保留。

## 整改对照

| 检查意见／补充要求 | 当前实现 | 核对入口 |
|---|---|---|
| 本轮功能超范围，导入和权限审核不应占据基础模型 | 活跃代码与页面只保留身份、组织、用户、角色、应用、菜单按钮、API资源、授权；移除导入、审批、复核、HR/OA、OIDC、会话管理API | [实际47个HTTP操作](routes.json)，[PRODUCT](../../PRODUCT.md) |
| 核心ER不能把每一张技术表都当业务实体 | 六类概念对象与十张物理表分开；账号凭证拆表不增加一个人员概念，三张关联表实现多对多 | [核心ER](../design/images/er-core.png)，[物理ER](../design/physical-er.mmd) |
| 前端菜单按钮与后端API混为一体 | Permission只表示MENU/BUTTON，ApiResource表示方法与路径；PermissionApi独立绑定 | [PermissionService](../../src/main/java/com/cdwy/permission/service/permission/PermissionService.java)，[ApiResourceService](../../src/main/java/com/cdwy/permission/service/resource/ApiResourceService.java) |
| 菜单应有树形结构 | parent_id自关联；同应用菜单才可作为父节点；防环；按钮必须挂菜单 | [权限服务类图](../design/images/class-permission.png) |
| 登录改为Token形式 | 登录签发Bearer JWT；无登录会话表；当前用户状态与credentialVersion实时核验 | [AuthService](../../src/main/java/com/cdwy/permission/service/auth/AuthService.java)，[JwtTokenService](../../src/main/java/com/cdwy/permission/service/auth/JwtTokenService.java) |
| 状态修改不能混在普通编辑中 | 用户、角色、应用、前端权限、API资源各有独立/status；夹带状态字段返回400；界面独立按钮及影响确认 | [API设计](api-design.md)，[前端](../../frontend/src/App.vue) |
| 包组织不统一，Service过大、Controller访问数据库 | 先controller/service/repository/entity/dto分层，再按auth/user/role等领域分包；10个Spring Service；Repository全用JPA | [架构说明](architecture.md) |
| 外部／临时人员不要再造独立人员模型 | UserAccount复用personType、expiresAt，默认外部365天／临时90天上限可配置；非正式人员必须设有效期 | [UserService](../../src/main/java/com/cdwy/permission/service/user/UserService.java)，[配置](../../src/main/resources/application.properties) |
| 已实现正确的多对多、JPA、CloneFactory应保留 | 用户角色、角色权限、权限API三组多对多；@CopyTo创建用户、@CopyFrom输出用户；密码与管理员身份不从普通DTO透传 | [实体映射](database-design.md)，[CloneFactory使用](architecture.md#clonefactory) |
| 不把课程鉴权示例写成已完成课程系统对接 | 只保留静态课程示例，控制器注明边界，页面明示示例数据 | [CourseDemoController](../../src/main/java/com/cdwy/permission/controller/demo/CourseDemoController.java) |

## 材料入口

- [需求与追踪](requirements.md)：功能、非功能、页面→API→Service→实体关系。
- [架构设计](architecture.md)：层次、模块、10个Service、事务及认证授权流程。
- [API设计](api-design.md)、[可导入Postman的OpenAPI](openapi.json)。
- [数据库设计](database-design.md)、[MySQL新库脚本](database-mysql.sql)、[实际JPA模型](schema.json)。
- [图与界面截图](../design/README.md)：2张类图、3张顺序图、核心ER、全表物理ER源。
- [运行演示步骤](demo.md)、[测试记录](testing.md)、[机器可读验证数据](verification.json)。

## 本轮验收

使用JDK21构建；在本机内存H2完成HTTP业务验收与独立浏览器操作。场景包括缺Token、错误密码、越权、三组关联缺失、同一Token下赋权与撤权、状态失效、菜单树校验、改密与停用后的旧Token失效。

SQL脚本仅在隔离的H2 MySQL兼容模式验证结构，与JPA逐表对比主键、唯一键、外键、列类型、长度和可空性。真实MySQL、旧库数据迁移与外部课程系统联调仍待实施；服务器部署需按目标环境另行完成。
