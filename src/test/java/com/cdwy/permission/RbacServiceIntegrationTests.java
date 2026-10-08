package com.cdwy.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cdwy.permission.dto.CreatePermissionRequest;
import com.cdwy.permission.dto.CreateRoleRequest;
import com.cdwy.permission.dto.CreateSystemRequest;
import com.cdwy.permission.dto.CreateUserRequest;
import com.cdwy.permission.dto.LoginResponse;
import com.cdwy.permission.dto.LoginRequest;
import com.cdwy.permission.dto.SetPasswordRequest;
import com.cdwy.permission.dto.UserResponse;
import com.cdwy.permission.exception.ForbiddenException;
import com.cdwy.permission.service.RbacService;
import com.cdwy.permission.service.SessionTokenService;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RbacServiceIntegrationTests {

    @Autowired
    private RbacService rbacService;

    @Autowired
    private SessionTokenService sessionTokenService;

    @Test
    void rolesAreMergedAndRevocationTakesEffectImmediately() {
        UserResponse user = rbacService.createUser(new CreateUserRequest("lisi", "李四"));
        var system = rbacService.createSystem(new CreateSystemRequest("finance", "财务系统", null));
        var viewer = rbacService.createRole(new CreateRoleRequest("viewer", "查看员", null));
        var exporter = rbacService.createRole(new CreateRoleRequest("exporter", "导出员", null));
        var listPermission = rbacService.createPermission(
                new CreatePermissionRequest(system.id(), "expense:list", "查看报销", "/expense/list"));
        var exportPermission = rbacService.createPermission(
                new CreatePermissionRequest(system.id(), "expense:export", "导出报销", "/expense/export"));

        rbacService.assignPermission(viewer.id(), listPermission.id());
        rbacService.assignPermission(exporter.id(), exportPermission.id());
        rbacService.assignRole(user.id(), viewer.id());
        rbacService.assignRole(user.id(), exporter.id());

        Set<String> mergedCodes = rbacService.effectivePermissions(user.id()).stream()
                .map(permission -> permission.code())
                .collect(Collectors.toSet());
        assertEquals(Set.of("expense:list", "expense:export"), mergedCodes);
        assertTrue(rbacService.hasPermission(user.id(), "expense:list"));

        rbacService.setPassword(user.id(), new SetPasswordRequest("password-123"));
        LoginResponse login = rbacService.login(new LoginRequest("lisi", "password-123"));
        assertTrue(login.accessToken() != null && !login.accessToken().isBlank());
        assertEquals(user.id(), login.userId());
        assertEquals(2, login.permissions().size());
        assertEquals(user.id(), sessionTokenService.requireUserId("Bearer " + login.accessToken()));

        rbacService.removeRole(user.id(), viewer.id());
        assertFalse(rbacService.hasPermission(user.id(), "expense:list"));
        assertTrue(rbacService.hasPermission(user.id(), "expense:export"));
        org.junit.jupiter.api.Assertions.assertThrows(
                ForbiddenException.class,
                () -> rbacService.assertPermission(user.id(), "expense:list"));

        sessionTokenService.revoke("Bearer " + login.accessToken());
        org.junit.jupiter.api.Assertions.assertThrows(
                com.cdwy.permission.exception.AuthenticationFailedException.class,
                () -> sessionTokenService.requireUserId("Bearer " + login.accessToken()));
    }
}
