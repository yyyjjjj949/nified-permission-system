package com.cdwy.permission.service.rbac;

import com.cdwy.permission.dto.permission.PermissionDtos;
import com.cdwy.permission.dto.role.RoleDtos;
import com.cdwy.permission.entity.permission.Permission;
import com.cdwy.permission.entity.rbac.*;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.rbac.*;
import com.cdwy.permission.repository.resource.ApiResourceRepository;
import com.cdwy.permission.service.permission.PermissionService;
import com.cdwy.permission.service.role.RoleService;
import com.cdwy.permission.service.user.UserService;
import java.time.Instant;
import java.util.*;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.pattern.*;

/** RBAC0：精确授权取并集，API 通过 PermissionApi 映射；导航祖先只作容器，不自动授予。 */
@Service
@Transactional(readOnly = true)
public class AuthorizationService {
  private final UserRoleRepository userRoles;
  private final RolePermissionRepository rolePermissions;
  private final PermissionApiRepository bindings;
  private final ApiResourceRepository resources;
  private final UserService users;
  private final RoleService roles;
  private final PermissionService permissions;

  public AuthorizationService(
      UserRoleRepository userRoles,
      RolePermissionRepository rolePermissions,
      PermissionApiRepository bindings,
      ApiResourceRepository resources,
      UserService users,
      RoleService roles,
      PermissionService permissions) {
    this.userRoles = userRoles;
    this.rolePermissions = rolePermissions;
    this.bindings = bindings;
    this.resources = resources;
    this.users = users;
    this.roles = roles;
    this.permissions = permissions;
  }

  @Transactional
  public void grantRole(Long userId, Long roleId) {
    var user = users.require(userId);
    var role = roles.require(roleId);
    if (!user.usable(Instant.now()) || !role.getEnabled())
      throw new IllegalArgumentException("不能给失效账号或停用角色新增授权");
    if (!userRoles.existsByUser_IdAndRole_Id(userId, roleId))
      userRoles.save(new UserRole(user, role));
  }

  @Transactional
  public void revokeRole(Long userId, Long roleId) {
    users.require(userId);
    roles.require(roleId);
    userRoles.deleteByUser_IdAndRole_Id(userId, roleId);
  }

  @Transactional
  public void grantPermission(Long roleId, Long permissionId) {
    var role = roles.require(roleId);
    var p = permissions.require(permissionId);
    if (!role.getEnabled() || !PermissionService.usable(p))
      throw new IllegalArgumentException("不能新增失效角色或权限的授权");
    if (!rolePermissions.existsByRole_IdAndPermission_Id(roleId, permissionId))
      rolePermissions.save(new RolePermission(role, p));
  }

  @Transactional
  public void revokePermission(Long roleId, Long permissionId) {
    roles.require(roleId);
    permissions.require(permissionId);
    rolePermissions.deleteByRole_IdAndPermission_Id(roleId, permissionId);
  }

  public List<RoleDtos.View> roles(Long userId) {
    users.require(userId);
    return userRoles.findAllByUser_Id(userId).stream()
        .map(g -> RoleService.view(g.getRole()))
        .toList();
  }

  public List<PermissionDtos.View> rolePermissions(Long roleId) {
    roles.require(roleId);
    return rolePermissions.findAllByRole_Id(roleId).stream()
        .map(g -> PermissionService.view(g.getPermission()))
        .toList();
  }

  public List<Permission> effective(Long userId) {
    if (!users.require(userId).usable(Instant.now())) return List.of();
    Map<Long, Permission> result = new LinkedHashMap<>();
    for (var grant : userRoles.findAllByUser_Id(userId))
      if (grant.getRole().getEnabled())
        for (var assignment : rolePermissions.findAllByRole_Id(grant.getRole().getId())) {
          Permission permission = assignment.getPermission();
          if (PermissionService.usable(permission))
            result.putIfAbsent(permission.getId(), permission);
        }
    return List.copyOf(result.values());
  }

  public List<PermissionDtos.View> effectiveViews(Long id) {
    return effective(id).stream().map(PermissionService::view).toList();
  }

  public List<PermissionDtos.Tree> menu(Long id) {
    return PermissionService.tree(effective(id));
  }

  public void requireApi(Long userId, String systemCode, String method, String path) {
    var candidates =
        resources.findAllBySystem_CodeAndMethod(systemCode, method).stream()
            .filter(r -> r.getSystem().getEnabled())
            .filter(
                r ->
                    PathPatternParser.defaultInstance
                        .parse(r.getPath())
                        .matches(PathContainer.parsePath(path)))
            .sorted(
                (a, b) ->
                    PathPattern.SPECIFICITY_COMPARATOR.compare(
                        PathPatternParser.defaultInstance.parse(a.getPath()),
                        PathPatternParser.defaultInstance.parse(b.getPath())))
            .toList();
    if (candidates.isEmpty()) throw new ForbiddenException("此接口尚未注册为有效 API 资源");
    // Prefer the most specific pattern. A broader grant cannot bypass a more specific resource.
    var selected = candidates.getFirst();
    if (!selected.getActive()) throw new ForbiddenException("此 API 资源已停用");
    Set<Long> effectiveIds = new HashSet<>();
    effective(userId).forEach(p -> effectiveIds.add(p.getId()));
    boolean allowed =
        bindings.findAllByResource_Id(selected.getId()).stream()
            .anyMatch(b -> effectiveIds.contains(b.getPermission().getId()));
    if (!allowed) throw new ForbiddenException("当前用户没有此 API 资源的访问权限");
  }
}
