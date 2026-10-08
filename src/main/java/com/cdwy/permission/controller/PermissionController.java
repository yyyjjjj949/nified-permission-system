package com.cdwy.permission.controller;

import com.cdwy.permission.dto.CreatePermissionRequest;
import com.cdwy.permission.dto.PermissionResponse;
import com.cdwy.permission.dto.UpdatePermissionRequest;
import com.cdwy.permission.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final RbacService rbacService;

    public PermissionController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createPermission(request));
    }

    @GetMapping
    public List<PermissionResponse> list(@RequestParam(required = false) Long systemId) {
        return rbacService.listPermissions(systemId);
    }

    @PutMapping("/{permissionId}")
    public PermissionResponse update(
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return rbacService.updatePermission(permissionId, request);
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> delete(@PathVariable Long permissionId) {
        rbacService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}
