package com.cdwy.permission;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cdwy.permission.config.*;
import com.cdwy.permission.repository.rbac.*;
import com.cdwy.permission.repository.user.UserAccountRepository;
import com.cdwy.permission.service.auth.AdminBootstrapService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** HTTP 级业务验收：相同 Token 下的授权变化、实际资源映射与无会话登录。 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:review_tests;DB_CLOSE_DELAY=-1",
      "permission.jwt.secret=first-review-integration-signing-secret-32-bytes",
      "permission.policy.external-max-days=7",
      "permission.policy.temporary-max-days=3"
    })
@AutoConfigureMockMvc
class FirstReviewWorkflowTests {
  static final String PASSWORD = "Review-2026!";
  @Autowired MockMvc mvc;
  @Autowired JsonMapper json;
  @Autowired JdbcTemplate db;
  @Autowired AdminBootstrapService bootstrap;
  @Autowired UserAccountRepository users;
  @Autowired ApplicationContext context;
  @Autowired RequestMappingHandlerMapping routes;
  String admin, employee;
  long user, system, role, menu, button, resource;

  @BeforeEach
  void fixture() throws Exception {
    db.execute("SET REFERENTIAL_INTEGRITY FALSE");
    for (String table :
        List.of(
            "permission_api",
            "sys_role_permission",
            "sys_user_role",
            "api_resource",
            "sys_permission",
            "sys_account",
            "sys_user",
            "sys_role",
            "biz_system",
            "sys_department")) db.update("delete from " + table);
    db.execute("SET REFERENTIAL_INTEGRITY TRUE");
    bootstrap.initialize("admin", PASSWORD);
    admin = login("admin", PASSWORD);
    user =
        create(
            "/api/users", Map.of("username", "worker", "displayName", "员工", "password", PASSWORD));
    employee = login("worker", PASSWORD);
    system = create("/api/systems", Map.of("code", "course", "name", "课程管理系统"));
    role = create("/api/roles", Map.of("code", "teacher", "name", "教师"));
    menu =
        create(
            "/api/permissions",
            Map.of(
                "systemId",
                system,
                "code",
                "course:menu",
                "name",
                "课程管理",
                "type",
                "MENU",
                "menuPath",
                "/courses"));
    button =
        create(
            "/api/permissions",
            Map.of(
                "systemId",
                system,
                "code",
                "course:list",
                "name",
                "查看课程",
                "type",
                "BUTTON",
                "parentId",
                menu));
    resource =
        create(
            "/api/resources",
            Map.of(
                "systemId", system, "name", "课程列表", "method", "GET", "path", "/api/demo/courses"));
  }

  JsonNode result(MvcResult result) {
    return json.readTree(
        new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
  }

  MockHttpServletRequestBuilder send(String method, String path, String token, Object body) {
    var b =
        request(org.springframework.http.HttpMethod.valueOf(method), path)
            .contentType(MediaType.APPLICATION_JSON);
    if (token != null) b.header("Authorization", "Bearer " + token);
    if (body != null) b.content(json.writeValueAsString(body));
    return b;
  }

  MvcResult call(String method, String path, String token, Object body, int status)
      throws Exception {
    return mvc.perform(send(method, path, token, body)).andExpect(status().is(status)).andReturn();
  }

  long create(String path, Object input) throws Exception {
    return result(call("POST", path, admin, input, 201)).get("id").asLong();
  }

  String login(String name, String password) throws Exception {
    return result(
            call(
                "POST",
                "/api/auth/login",
                null,
                Map.of("username", name, "password", password),
                200))
        .get("accessToken")
        .asString();
  }

  void grant() throws Exception {
    call("PUT", "/api/permissions/" + button + "/resources/" + resource, admin, null, 204);
    call("PUT", "/api/roles/" + role + "/permissions/" + button, admin, null, 204);
    call("PUT", "/api/users/" + user + "/roles/" + role, admin, null, 204);
  }

  void demo(int expected) throws Exception {
    call("GET", "/api/demo/courses", employee, null, expected);
  }

  @Test
  void loginReturnsBearerAndNeverCreatesServerSession() throws Exception {
    var response =
        call(
            "POST",
            "/api/auth/login",
            null,
            Map.of("username", "worker", "password", PASSWORD),
            200);
    assertEquals("Bearer", result(response).get("tokenType").asString());
    assertTrue(result(response).get("expiresIn").asLong() > 0);
    assertNull(response.getRequest().getSession(false));
    assertNull(response.getResponse().getHeader("Set-Cookie"));
    // Hibernate may create temporary bulk-mutation tables for @SecondaryTable; these are not
    // persisted business tables.
    Set<String> tables =
        new HashSet<>(
            db.queryForList(
                "select table_name from information_schema.tables where table_schema='PUBLIC' and"
                    + " table_type='BASE TABLE'",
                String.class));
    assertEquals(10, tables.size());
    assertFalse(tables.stream().anyMatch(t -> t.toLowerCase().contains("session")));
    call("GET", "/api/me", employee, null, 200);
  }

  @Test
  void badCredentialsMissingMalformedExpiredAndForgedTokensReturn401() throws Exception {
    call("POST", "/api/auth/login", null, Map.of("username", "worker", "password", "wrong"), 401);
    call("POST", "/api/auth/login", null, Map.of("username", "missing", "password", PASSWORD), 401);
    call("GET", "/api/me", null, null, 401);
    call("GET", "/api/me", "not-a-jwt", null, 401);
    var key =
        Keys.hmacShaKeyFor(
            "first-review-integration-signing-secret-32-bytes".getBytes(StandardCharsets.UTF_8));
    String expired =
        Jwts.builder()
            .subject("" + user)
            .claim("cv", 0)
            .issuedAt(Date.from(Instant.now().minusSeconds(60)))
            .expiration(Date.from(Instant.now().minusSeconds(1)))
            .signWith(key)
            .compact();
    call("GET", "/api/me", expired, null, 401);
    String forged =
        Jwts.builder()
            .subject("" + user)
            .claim("cv", 0)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(Jwts.SIG.HS256.key().build())
            .compact();
    call("GET", "/api/me", forged, null, 401);
  }

  @Test
  void loginResponseCannotExposePasswordOrCredentialVersionOrAdminAssignment() throws Exception {
    String body =
        call(
                "POST",
                "/api/auth/login",
                null,
                Map.of("username", "worker", "password", PASSWORD),
                200)
            .getResponse()
            .getContentAsString();
    assertFalse(body.contains("passwordHash"));
    assertFalse(body.contains("credentialVersion"));
    call(
        "POST",
        "/api/users",
        admin,
        Map.of(
            "username",
            "escalate",
            "displayName",
            "不能提权",
            "password",
            PASSWORD,
            "administrator",
            true),
        400);
  }

  @Test
  void ordinaryUsersCannotManageAndAdministratorHasNoAutomaticBusinessGrant() throws Exception {
    call("GET", "/api/users", employee, null, 403);
    call("POST", "/api/roles", employee, Map.of("code", "hack", "name", "不能创建"), 403);
    call("GET", "/api/demo/courses", admin, null, 403);
  }

  @Test
  void rolePermissionAndApiBindingAreAllNecessaryAndNewGrantsWorkWithoutRelogin() throws Exception {
    demo(403);
    call("PUT", "/api/users/" + user + "/roles/" + role, admin, null, 204);
    demo(403);
    call("PUT", "/api/roles/" + role + "/permissions/" + button, admin, null, 204);
    demo(403);
    call("PUT", "/api/permissions/" + button + "/resources/" + resource, admin, null, 204);
    demo(200);
  }

  @Test
  void duplicateAssignmentsAreIdempotentAndGrantUnionSurvivesOneRoleRevocation() throws Exception {
    grant();
    grant();
    assertEquals(1, db.queryForObject("select count(*) from sys_user_role", Integer.class));
    assertEquals(1, db.queryForObject("select count(*) from permission_api", Integer.class));
    long second = create("/api/roles", Map.of("code", "assistant", "name", "助教"));
    call("PUT", "/api/users/" + user + "/roles/" + second, admin, null, 204);
    call("PUT", "/api/roles/" + second + "/permissions/" + button, admin, null, 204);
    call("DELETE", "/api/users/" + user + "/roles/" + role, admin, null, 204);
    demo(200);
    call("DELETE", "/api/users/" + user + "/roles/" + second, admin, null, 204);
    demo(403);
  }

  @Test
  void permissionRevocationAndApiUnbindingImmediatelyRejectExistingToken() throws Exception {
    grant();
    demo(200);
    call("DELETE", "/api/permissions/" + button + "/resources/" + resource, admin, null, 204);
    demo(403);
    grant();
    demo(200);
    call("DELETE", "/api/roles/" + role + "/permissions/" + button, admin, null, 204);
    demo(403);
  }

  @Test
  void statusCommandsAffectAllHoldersWithoutChangingOrdinaryFields() throws Exception {
    grant();
    demo(200);
    for (String route :
        List.of(
            "/api/permissions/" + button,
            "/api/permissions/" + menu,
            "/api/resources/" + resource)) {
      call("PUT", route + "/status", admin, Map.of("active", false), 200);
      demo(403);
      call("PUT", route + "/status", admin, Map.of("active", true), 200);
      demo(200);
    }
    for (String route : List.of("/api/roles/" + role, "/api/systems/" + system)) {
      call("PUT", route + "/status", admin, Map.of("enabled", false), 200);
      demo(403);
      call("PUT", route + "/status", admin, Map.of("enabled", true), 200);
      demo(200);
    }
  }

  @Test
  void ordinaryEditsCannotSmuggleStatusAndNamesDoNotRevokeGrants() throws Exception {
    grant();
    call(
        "PUT",
        "/api/permissions/" + button,
        admin,
        Map.of("name", "查看课程", "type", "BUTTON", "parentId", menu, "active", false),
        400);
    call("PUT", "/api/roles/" + role, admin, Map.of("name", "教师", "enabled", false), 400);
    call("PUT", "/api/systems/" + system, admin, Map.of("name", "课程", "enabled", false), 400);
    call(
        "PUT",
        "/api/users/" + user,
        admin,
        Map.of("displayName", "员工", "personType", "EMPLOYEE", "enabled", false),
        400);
    call(
        "PUT",
        "/api/permissions/" + button,
        admin,
        Map.of("name", "查看课程列表", "type", "BUTTON", "parentId", menu),
        200);
    demo(200);
  }

  @Test
  void disabledAccountInvalidatesTokenAndReenableDoesNotReviveIt() throws Exception {
    call("PUT", "/api/users/" + user + "/status", admin, Map.of("enabled", false), 200);
    call("GET", "/api/me", employee, null, 401);
    call("POST", "/api/auth/login", null, Map.of("username", "worker", "password", PASSWORD), 401);
    call("PUT", "/api/users/" + user + "/status", admin, Map.of("enabled", true), 200);
    call("GET", "/api/me", employee, null, 401);
    call("GET", "/api/me", login("worker", PASSWORD), null, 200);
  }

  @Test
  void passwordChangeInvalidatesOldTokenWithoutSessionStorage() throws Exception {
    call(
        "PUT",
        "/api/me/password",
        employee,
        Map.of("currentPassword", "wrong", "newPassword", "New-Password!2026"),
        401);
    call(
        "PUT",
        "/api/me/password",
        employee,
        Map.of("currentPassword", PASSWORD, "newPassword", "New-Password!2026"),
        204);
    call("GET", "/api/me", employee, null, 401);
    call("POST", "/api/auth/login", null, Map.of("username", "worker", "password", PASSWORD), 401);
    call("GET", "/api/me", login("worker", "New-Password!2026"), null, 200);
  }

  @Test
  void menuTreeKeepsAncestorsAsContainersWithoutGrantingTheirApi() throws Exception {
    grant();
    var tree = result(call("GET", "/api/me/menu", employee, null, 200));
    assertEquals(menu, tree.get(0).get("permission").get("id").asLong());
    assertFalse(tree.get(0).get("granted").asBoolean());
    assertTrue(tree.get(0).get("children").get(0).get("granted").asBoolean());
    assertEquals(1, result(call("GET", "/api/me/permissions", employee, null, 200)).size());
  }

  @Test
  void menuTreeRejectsCyclesCrossApplicationParentsAndButtonParents() throws Exception {
    call(
        "PUT",
        "/api/permissions/" + menu,
        admin,
        Map.of("name", "课程", "type", "MENU", "parentId", menu),
        400);
    call(
        "PUT",
        "/api/permissions/" + menu,
        admin,
        Map.of("name", "课程", "type", "BUTTON", "parentId", button),
        400);
    long other = create("/api/systems", Map.of("code", "other", "name", "其他"));
    call(
        "POST",
        "/api/permissions",
        admin,
        Map.of(
            "systemId",
            other,
            "code",
            "other:button",
            "name",
            "按钮",
            "type",
            "BUTTON",
            "parentId",
            menu),
        400);
    call(
        "POST",
        "/api/permissions",
        admin,
        Map.of("systemId", system, "code", "bad:button", "name", "按钮", "type", "BUTTON"),
        400);
    call(
        "POST",
        "/api/permissions",
        admin,
        Map.of("systemId", system, "code", "bad:api", "name", "API", "type", "API"),
        400);
  }

  @Test
  void apiResourcesCannotBindAcrossApplicationsOrBypassMoreSpecificRoute() throws Exception {
    grant();
    long other = create("/api/systems", Map.of("code", "other", "name", "其他"));
    long otherApi =
        create(
            "/api/resources",
            Map.of("systemId", other, "name", "其他接口", "method", "GET", "path", "/api/other"));
    call("PUT", "/api/permissions/" + button + "/resources/" + otherApi, admin, null, 400);
    long variable =
        create(
            "/api/resources",
            Map.of(
                "systemId",
                system,
                "name",
                "泛化路径",
                "method",
                "GET",
                "path",
                "/api/demo/{anything}"));
    call("PUT", "/api/permissions/" + button + "/resources/" + variable, admin, null, 204);
    call("DELETE", "/api/permissions/" + button + "/resources/" + resource, admin, null, 204);
    demo(403);
    call("PUT", "/api/permissions/" + button + "/resources/" + resource, admin, null, 204);
    demo(200);
    // Disabling an exact resource must not fall back to an authorized broader pattern.
    call("PUT", "/api/resources/" + resource + "/status", admin, Map.of("active", false), 200);
    demo(403);
    call("PUT", "/api/resources/" + resource + "/status", admin, Map.of("active", true), 200);
    demo(200);
  }

  @Test
  void resourceValidationRejectsDuplicatesAndBroadWildcards() throws Exception {
    call(
        "POST",
        "/api/resources",
        admin,
        Map.of("systemId", system, "name", "重复", "method", "GET", "path", "/api/demo/courses"),
        409);
    call(
        "POST",
        "/api/resources",
        admin,
        Map.of("systemId", system, "name", "过宽", "method", "GET", "path", "/api/**"),
        400);
    call(
        "POST",
        "/api/resources",
        admin,
        Map.of("systemId", system, "name", "无绝对路径", "method", "GET", "path", "api/demo"),
        400);
  }

  @Test
  void departmentTreeAndDeleteReferencesRemainConsistent() throws Exception {
    long root = create("/api/departments", Map.of("name", "信息部"));
    long child = create("/api/departments", Map.of("name", "开发组", "parentId", root));
    call("PUT", "/api/departments/" + root, admin, Map.of("name", "信息部", "parentId", child), 400);
    call("DELETE", "/api/departments/" + root, admin, null, 409);
    call(
        "PUT",
        "/api/users/" + user,
        admin,
        Map.of("displayName", "员工", "departmentId", child, "personType", "EMPLOYEE"),
        200);
    call("DELETE", "/api/departments/" + child, admin, null, 409);
  }

  @Test
  void externalAndTemporaryPeopleUseSharedUserEntityAndConfiguredValidityLimits() throws Exception {
    var today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
    call(
        "POST",
        "/api/users",
        admin,
        Map.of(
            "username",
            "external",
            "displayName",
            "外部",
            "password",
            PASSWORD,
            "personType",
            "EXTERNAL"),
        400);
    call(
        "POST",
        "/api/users",
        admin,
        Map.of(
            "username",
            "external",
            "displayName",
            "外部",
            "password",
            PASSWORD,
            "personType",
            "EXTERNAL",
            "expiresOn",
            today.plusDays(8).toString()),
        400);
    long external =
        create(
            "/api/users",
            Map.of(
                "username",
                "external",
                "displayName",
                "外部",
                "password",
                PASSWORD,
                "personType",
                "EXTERNAL",
                "expiresOn",
                today.plusDays(7).toString()));
    assertEquals(
        "EXTERNAL",
        result(call("GET", "/api/users/" + external, admin, null, 200))
            .get("personType")
            .asString());
    call(
        "POST",
        "/api/users",
        admin,
        Map.of(
            "username",
            "temp",
            "displayName",
            "临时",
            "password",
            PASSWORD,
            "personType",
            "TEMPORARY",
            "expiresOn",
            today.plusDays(4).toString()),
        400);
    db.update(
        "update sys_user set expires_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(1)),
        external);
    call(
        "POST", "/api/auth/login", null, Map.of("username", "external", "password", PASSWORD), 401);
  }

  @Test
  void deletionsRespectReferencesAndProtectAdministrator() throws Exception {
    grant();
    call("DELETE", "/api/roles/" + role, admin, null, 409);
    call("DELETE", "/api/permissions/" + button, admin, null, 409);
    call("DELETE", "/api/resources/" + resource, admin, null, 409);
    call("DELETE", "/api/systems/" + system, admin, null, 409);
    long adminId = result(call("GET", "/api/me", admin, null, 200)).get("id").asLong();
    call("PUT", "/api/users/" + adminId + "/status", admin, Map.of("enabled", false), 403);
    call("DELETE", "/api/users/" + adminId, admin, null, 403);
    call("DELETE", "/api/users/" + user, admin, null, 204);
    call("GET", "/api/me", employee, null, 401);
  }

  @Test
  void initializationCannotEscalateOrdinaryUserAndRemovedEndpointsAreNotRegistered() {
    assertThrows(IllegalStateException.class, () -> bootstrap.initialize("worker", PASSWORD));
    var registered =
        routes.getHandlerMethods().keySet().stream()
            .flatMap(r -> r.getPatternValues().stream())
            .toList();
    for (String old :
        List.of(
            "imports",
            "reviews",
            "sessions",
            "approvals",
            "integration",
            "oidc",
            "refresh",
            "offline")) assertTrue(registered.stream().noneMatch(p -> p.contains(old)), old);
    assertFalse(context.containsBean("loginSessionRepository"));
  }

  @Test
  void exportOpenApiInputsRoutesAndActualSchemaForDocumentation() throws Exception {
    var schemas = new LinkedHashMap<String, List<Map<String, Object>>>();
    for (String table :
        db.queryForList(
            "select table_name from information_schema.tables where table_schema='PUBLIC' and"
                + " table_type='BASE TABLE' order by table_name",
            String.class))
      schemas.put(
          table,
          db.queryForList(
              "select column_name,data_type,is_nullable from information_schema.columns where"
                  + " table_schema='PUBLIC' and table_name=? order by ordinal_position",
              table));
    var exported =
        routes.getHandlerMethods().entrySet().stream()
            .filter(
                e ->
                    e.getValue()
                        .getBeanType()
                        .getPackageName()
                        .startsWith("com.cdwy.permission.controller"))
            .flatMap(
                e ->
                    e.getKey().getPatternValues().stream()
                        .map(
                            p ->
                                Map.of(
                                    "path",
                                    p,
                                    "methods",
                                    e.getKey().getMethodsCondition().getMethods().stream()
                                        .map(Enum::name)
                                        .toList(),
                                    "controller",
                                    e.getValue().getBeanType().getSimpleName(),
                                    "method",
                                    e.getValue().getMethod().getName())))
            .toList();
    java.nio.file.Path out = java.nio.file.Path.of("target/first-review");
    java.nio.file.Files.createDirectories(out);
    java.nio.file.Files.writeString(
        out.resolve("schema.json"),
        json.writerWithDefaultPrettyPrinter().writeValueAsString(schemas));
    java.nio.file.Files.writeString(
        out.resolve("routes.json"),
        json.writerWithDefaultPrettyPrinter().writeValueAsString(exported));
    db.execute("SCRIPT NODATA TO 'target/first-review/schema-h2.sql'");
  }

  @Test
  void mysqlDdlMatchesPersistentTablesColumnsKeysAndUniqueConstraints() throws Exception {
    // Structural validation on an isolated H2 MySQL-mode database; not a real MySQL certification.
    String ddl =
        java.nio.file.Files.readString(
                java.nio.file.Path.of("docs/first-review/database-mysql.sql"))
            .replaceAll("(?i)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs", "");
    try (var connection =
        java.sql.DriverManager.getConnection(
            "jdbc:h2:mem:ddl_compatibility;MODE=MySQL", "sa", "")) {
      for (String sql : ddl.replaceAll("(?m)--[^\\r\\n]*", "").split(";"))
        if (!sql.isBlank())
          try (var statement = connection.createStatement()) {
            statement.execute(sql);
          }
      var model = db.getDataSource().getConnection();
      try (model) {
        var expected = model.getMetaData();
        var actual = connection.getMetaData();
        Set<String> tables =
            new TreeSet<>(
                db.queryForList(
                    "select table_name from information_schema.tables where table_schema='PUBLIC'"
                        + " and table_type='BASE TABLE'",
                    String.class));
        var actualTables = new TreeSet<String>();
        try (var rows = actual.getTables(null, "PUBLIC", "%", new String[] {"BASE TABLE"})) {
          while (rows.next()) actualTables.add(rows.getString("TABLE_NAME"));
        }
        assertEquals(tables, actualTables);
        int foreignKeys = 0;
        for (String table : tables) {
          assertEquals(columns(expected, table), columns(actual, table), "Columns: " + table);
          assertEquals(keys(expected, table), keys(actual, table), "Keys: " + table);
          try (var rows = actual.getImportedKeys(null, "PUBLIC", table)) {
            while (rows.next()) foreignKeys++;
          }
        }
        assertEquals(12, foreignKeys);
      }
    }
  }

  private Map<String, String> columns(java.sql.DatabaseMetaData metadata, String table)
      throws Exception {
    var result = new TreeMap<String, String>();
    try (var rows = metadata.getColumns(null, "PUBLIC", table, "%")) {
      while (rows.next()) {
        String type = rows.getString("TYPE_NAME");
        if (type.startsWith("TIMESTAMP"))
          type = "TIMESTAMP"; // MySQL stores Instant as a UTC datetime.
        String size = type.equals("CHARACTER VARYING") ? ":" + rows.getInt("COLUMN_SIZE") : "";
        result.put(rows.getString("COLUMN_NAME"), type + size + ":" + rows.getInt("NULLABLE"));
      }
    }
    return result;
  }

  private Set<String> keys(java.sql.DatabaseMetaData metadata, String table) throws Exception {
    var result = new TreeSet<String>();
    try (var rows = metadata.getPrimaryKeys(null, "PUBLIC", table)) {
      while (rows.next()) result.add("PK:" + rows.getString("COLUMN_NAME"));
    }
    try (var rows = metadata.getImportedKeys(null, "PUBLIC", table)) {
      while (rows.next())
        result.add(
            "FK:"
                + rows.getString("FKCOLUMN_NAME")
                + "->"
                + rows.getString("PKTABLE_NAME")
                + "."
                + rows.getString("PKCOLUMN_NAME"));
    }
    var indexes = new TreeMap<String, List<String>>();
    try (var rows = metadata.getIndexInfo(null, "PUBLIC", table, true, false)) {
      while (rows.next())
        if (rows.getString("COLUMN_NAME") != null)
          indexes
              .computeIfAbsent(rows.getString("INDEX_NAME"), key -> new ArrayList<>())
              .add(rows.getString("COLUMN_NAME"));
    }
    indexes.values().forEach(columns -> result.add("UK:" + String.join(",", columns)));
    return result;
  }
}
