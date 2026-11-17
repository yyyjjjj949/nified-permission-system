-- 第一轮检查整改分支；MySQL 8.0.16+；请先选择一个全新的空 schema。
-- 本脚本不删除、覆盖或迁移旧库。不要对原 38 表数据库运行。
-- 10 张持久化表，12 个外键；JWT 不建立登录会话表。
-- 时间以 UTC 写入 datetime(6)，业务有效期由 PERMISSION_POLICY_ZONE 解释。

CREATE TABLE sys_department (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    parent_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_department_name UNIQUE (name),
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES sys_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(128) NOT NULL,
    department_id BIGINT NULL,
    person_type ENUM('EMPLOYEE','EXTERNAL','TEMPORARY') NOT NULL,
    expires_at DATETIME(6) NULL,
    enabled BIT(1) NOT NULL DEFAULT 1,
    administrator BIT(1) NOT NULL DEFAULT 0,
    credential_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES sys_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_account (
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_account_username UNIQUE (username),
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255) NULL,
    enabled BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE biz_system (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    base_url VARCHAR(255) NULL,
    enabled BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_system_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    system_id BIGINT NOT NULL,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(128) NOT NULL,
    type ENUM('BUTTON','MENU') NOT NULL,
    menu_path VARCHAR(255) NULL,
    parent_id BIGINT NULL,
    active BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_permission_code UNIQUE (code),
    CONSTRAINT fk_permission_system FOREIGN KEY (system_id) REFERENCES biz_system(id),
    CONSTRAINT fk_permission_parent FOREIGN KEY (parent_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE api_resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    system_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    method VARCHAR(8) NOT NULL,
    path VARCHAR(255) NOT NULL,
    active BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_api_route UNIQUE (system_id,method,path),
    CONSTRAINT fk_resource_system FOREIGN KEY (system_id) REFERENCES biz_system(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id,role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id,permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

CREATE TABLE permission_api (
    id BIGINT NOT NULL AUTO_INCREMENT,
    permission_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_permission_api UNIQUE (permission_id,resource_id),
    CONSTRAINT fk_permission_api_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id),
    CONSTRAINT fk_permission_api_resource FOREIGN KEY (resource_id) REFERENCES api_resource(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;
