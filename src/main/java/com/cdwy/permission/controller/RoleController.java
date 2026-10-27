package com.cdwy.permission.controller;

import com.cdwy.permission.dto.CreateRoleRequest;
import com.cdwy.permission.dto.PermissionResponse;
import com.cdwy.permission.dto.RoleResponse;
import com.cdwy.permission.dto.UpdateRoleRequest;
import com.cdwy.permission.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RbacService rbacService;

    public RoleController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createRole(request));
    }

    @GetMapping
    public List<RoleResponse> list() {
        return rbacService.listRoles();
    }

    @PutMapping("/{roleId}")
    public RoleResponse update(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return rbacService.updateRole(roleId, request);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> delete(@PathVariable Long roleId) {
        rbacService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> assignPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rbacService.assignPermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rbacService.removePermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}/permissions")
    public List<PermissionResponse> listPermissions(@PathVariable Long roleId) {
        return rbacService.listRolePermissions(roleId);
    }
}
