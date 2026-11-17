package com.cdwy.permission.controller.rbac;

import com.cdwy.permission.dto.permission.PermissionDtos;
import com.cdwy.permission.dto.role.RoleDtos;
import com.cdwy.permission.service.rbac.AuthorizationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GrantController {
  private final AuthorizationService service;

  public GrantController(AuthorizationService service) {
    this.service = service;
  }

  @GetMapping("/users/{userId}/roles")
  public List<RoleDtos.View> roles(@PathVariable Long userId) {
    return service.roles(userId);
  }

  @PutMapping("/users/{userId}/roles/{roleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void grantRole(@PathVariable Long userId, @PathVariable Long roleId) {
    service.grantRole(userId, roleId);
  }

  @DeleteMapping("/users/{userId}/roles/{roleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeRole(@PathVariable Long userId, @PathVariable Long roleId) {
    service.revokeRole(userId, roleId);
  }

  @GetMapping("/roles/{roleId}/permissions")
  public List<PermissionDtos.View> permissions(@PathVariable Long roleId) {
    return service.rolePermissions(roleId);
  }

  @PutMapping("/roles/{roleId}/permissions/{permissionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void grantPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
    service.grantPermission(roleId, permissionId);
  }

  @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
    service.revokePermission(roleId, permissionId);
  }
}
