# CloneFactory 集成说明

## 作用

CloneFactory 是随本项目一起保存的 Maven 注解处理器，坐标为
`cn.edu.xmu:clonefactory:0.0.1-SNAPSHOT`。它在编译阶段根据 DTO 上的注解生成
类型安全的属性拷贝方法，避免在业务服务中重复编写字段赋值代码。

实现源码位于 `third-party/clonefactory/src/main/java/cn/edu/xmu/clonefactory`。
主项目的 `pom.xml` 同时把 CloneFactory 声明为普通依赖和 annotation processor，
因此注解和生成器都会参与编译。

## 首次安装

当前主项目是 Spring Boot `jar` 工程，CloneFactory 作为内置依赖单独安装到本地
Maven 仓库，不加入 Maven reactor。首次构建或清空本地仓库后执行：

```powershell
$env:JAVA_HOME = 'C:\Users\姚景云\.jdks\corretto-21.0.12.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd D:\nified-permission-system\third-party\clonefactory
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" clean install
cd D:\nified-permission-system
mvn "-Dmaven.repo.local=D:\CodexData\cache\m2" clean test
```

如果项目检出到了其他目录，只需把命令中的项目根目录替换为实际路径。

## 注解用法

注解写在作为映射声明的一侧：

- `@CopyTo(UserAccount.class)` 写在 `CreateUserRequest` 上，生成
  `CloneFactory.copy(UserAccount target, CreateUserRequest source)`。
- `@CopyFrom(UserAccount.class)` 写在 `UserResponse` 上，生成
  `CloneFactory.copy(UserResponse target, UserAccount source)`。
- `@CopyNotNullTo(UserAccount.class)` 写在 `UpdateUserRequest` 上，只把请求中非空
  的属性复制到已有的 `UserAccount`。
- CloneFactory 也提供 `@CopyNotNullFrom`，用于从源对象向目标 DTO 只复制非空属性。

当前业务中的映射声明位于：

```text
src/main/java/com/cdwy/permission/dto/CreateUserRequest.java
src/main/java/com/cdwy/permission/dto/UpdateUserRequest.java
src/main/java/com/cdwy/permission/dto/UserResponse.java
src/main/java/com/cdwy/permission/dto/RoleResponse.java
src/main/java/com/cdwy/permission/dto/SystemResponse.java
src/main/java/com/cdwy/permission/dto/PermissionResponse.java
```

服务层调用示例：

```java
UserAccount user = CloneFactory.copy(
        new UserAccount(request.username(), request.displayName()), request);
CloneFactory.copyNotNull(user, request);
UserResponse response = CloneFactory.copy(new UserResponse(), user);
```

实体和 DTO 必须提供符合 JavaBean 命名规则的 `getXxx()` 与 `setXxx(...)` 方法，
否则处理器不会为对应属性生成拷贝语句。当前项目保留了原有的 record-style 方法，
同时为 CloneFactory 补充了标准 getter/setter。

## 生成结果和检查

执行 `mvn clean compile` 或 `mvn clean test` 后检查：

```text
target/generated-sources/annotations/cn/edu/xmu/clonefactory/util/CloneFactory.java
```

生成类应包含用户、角色、业务系统、权限响应的 `copy` 方法，以及用户创建和用户
更新使用的 `copy` / `copyNotNull` 方法。`target` 属于构建产物，不提交到 Git。
