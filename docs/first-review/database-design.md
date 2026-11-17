# 数据库设计（当前10表模型）

概念层是6类核心对象；物理层是10张持久化表。`sys_account`为用户凭证技术拆表，另外3张是多对多关联表。JWT不持久化，无登录会话表。

## 实体与物理映射

| 表 | 概念／职责 | 说明 |
|---|---|---|
| `api_resource` | API资源 | 应用+方法+路径组合唯一；与菜单、按钮不是同一实体。 |
| `biz_system` | 应用 | 应用目录；停用影响该应用所有权限和资源。 |
| `permission_api` | 权限API关联 | 前端权限与API资源M:N；两端应用一致由Service校验。 |
| `sys_account` | 用户登录凭证（技术拆表） | @SecondaryTable映射到同一个UserAccount实体；user_id同时为主键和外键，没有第二个独立人员实体。 |
| `sys_department` | 部门 | 部门树；parent_id可空，Service拒绝环。 |
| `sys_permission` | 前端权限 | 仅MENU/BUTTON；parent_id菜单树，code全局唯一。 |
| `sys_role` | 角色 | 业务权限集合；enabled控制是否计入有效权限。 |
| `sys_role_permission` | 角色权限关联 | 角色与前端权限M:N，组合唯一。 |
| `sys_user` | 用户 | 人员资料、人员身份、状态、有效期与账号级凭证版本。 |
| `sys_user_role` | 用户角色关联 | 用户与角色M:N，组合唯一。 |

## 字段字典

以下字段、类型、可空性取自测试启动后实际JPA模型；MySQL长度、约束与DDL见[建表脚本](database-mysql.sql)。H2枚举/时区时间与MySQL存储类型存在方言差异。

### api_resource

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `active` | BOOLEAN | 否 |
| `id` | BIGINT | 否 |
| `method` | CHARACTER VARYING | 否 |
| `system_id` | BIGINT | 否 |
| `name` | CHARACTER VARYING | 否 |
| `path` | CHARACTER VARYING | 否 |

### biz_system

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `enabled` | BOOLEAN | 否 |
| `id` | BIGINT | 否 |
| `code` | CHARACTER VARYING | 否 |
| `name` | CHARACTER VARYING | 否 |
| `base_url` | CHARACTER VARYING | 是 |

### permission_api

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `id` | BIGINT | 否 |
| `permission_id` | BIGINT | 否 |
| `resource_id` | BIGINT | 否 |

### sys_account

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `user_id` | BIGINT | 否 |
| `username` | CHARACTER VARYING | 否 |
| `password_hash` | CHARACTER VARYING | 否 |

### sys_department

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `id` | BIGINT | 否 |
| `parent_id` | BIGINT | 是 |
| `name` | CHARACTER VARYING | 否 |

### sys_permission

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `active` | BOOLEAN | 否 |
| `id` | BIGINT | 否 |
| `parent_id` | BIGINT | 是 |
| `system_id` | BIGINT | 否 |
| `code` | CHARACTER VARYING | 否 |
| `name` | CHARACTER VARYING | 否 |
| `menu_path` | CHARACTER VARYING | 是 |
| `type` | ENUM | 否 |

### sys_role

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `enabled` | BOOLEAN | 否 |
| `id` | BIGINT | 否 |
| `code` | CHARACTER VARYING | 否 |
| `name` | CHARACTER VARYING | 否 |
| `description` | CHARACTER VARYING | 是 |

### sys_role_permission

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `id` | BIGINT | 否 |
| `permission_id` | BIGINT | 否 |
| `role_id` | BIGINT | 否 |

### sys_user

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `administrator` | BOOLEAN | 否 |
| `enabled` | BOOLEAN | 否 |
| `credential_version` | BIGINT | 否 |
| `department_id` | BIGINT | 是 |
| `expires_at` | TIMESTAMP WITH TIME ZONE | 是 |
| `id` | BIGINT | 否 |
| `display_name` | CHARACTER VARYING | 否 |
| `person_type` | ENUM | 否 |

### sys_user_role

| 字段 | H2实际类型 | 可空 |
|---|---|---|
| `id` | BIGINT | 否 |
| `role_id` | BIGINT | 否 |
| `user_id` | BIGINT | 否 |

## 约束与事务

- 12个外键包含部门自关联、用户部门、账号用户、菜单自关联、权限应用、资源应用，以及3张关联表的6个外键。
- 登录名、部门名、角色编码、应用编码、权限编码唯一。API资源按`system_id/method/path`唯一。三张关联表各自具有组合唯一约束。
- 新建用户、改密、启停、赋权等写操作在Service事务中执行；账号敏感写操作使用JPA悲观行锁，版本更新不依赖HTTP Session。
- 删除用户时删除其用户角色关系；有引用的角色、应用、权限、资源、部门禁止直接删除并返回409，避免误删共享配置。
- 数据库外键保证引用存在；同应用、父节点类型、防环、有效期等业务约束由Service处理。前端校验只用于交互。
- MySQL脚本只用于新空库。旧版本数据迁移尚未实施；不要使用Hibernate update让旧库自动变形。
- DDL已通过隔离H2 MySQL模式结构对比（表、列、长度、可空、主键、唯一键、12外键）；这不等同于真实MySQL运行验证。
- H2内部`HT_SYS_USER`/`HTE_SYS_USER`是Hibernate临时bulk mutation表，不计入业务表；没有把它们作为核心实体。
- MySQL中到期时间使用UTC datetime(6)，Hibernate JDBC时区固定为UTC；有效期日期按可配置业务时区解释。

## 图

核心概念图见[核心ER](../design/images/er-core.png)，全10表结构见[物理ER](../design/images/er-physical.png)及[可编辑源](../design/physical-er.mmd)。
