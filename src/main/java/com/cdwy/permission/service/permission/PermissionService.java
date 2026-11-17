package com.cdwy.permission.service.permission;

import com.cdwy.permission.dto.permission.PermissionDtos;
import com.cdwy.permission.dto.resource.ResourceDtos;
import com.cdwy.permission.entity.permission.*;
import com.cdwy.permission.entity.rbac.PermissionApi;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.permission.PermissionRepository;
import com.cdwy.permission.repository.rbac.*;
import com.cdwy.permission.service.resource.ApiResourceService;
import com.cdwy.permission.service.system.SystemService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PermissionService {
  private final PermissionRepository permissions;
  private final RolePermissionRepository grants;
  private final PermissionApiRepository bindings;
  private final SystemService systems;
  private final ApiResourceService resources;

  public PermissionService(
      PermissionRepository permissions,
      RolePermissionRepository grants,
      PermissionApiRepository bindings,
      SystemService systems,
      ApiResourceService resources) {
    this.permissions = permissions;
    this.grants = grants;
    this.bindings = bindings;
    this.systems = systems;
    this.resources = resources;
  }

  public Permission require(Long id) {
    return permissions.findById(id).orElseThrow(() -> new ResourceNotFoundException("前端权限不存在"));
  }

  public static PermissionDtos.View view(Permission p) {
    return new PermissionDtos.View(
        p.getId(),
        p.getSystem().getId(),
        p.getCode(),
        p.getName(),
        p.getType().name(),
        p.getMenuPath(),
        p.getParent() == null ? null : p.getParent().getId(),
        p.getActive());
  }

  public List<PermissionDtos.View> list(Long systemId) {
    return (systemId == null
            ? permissions.findAll(org.springframework.data.domain.Sort.by("id"))
            : permissions.findAllBySystem_IdOrderById(systemId))
        .stream().map(PermissionService::view).toList();
  }

  private void apply(Permission p, String name, PermissionType type, String path, Long parentId) {
    Permission parent = parentId == null ? null : require(parentId);
    if (type == PermissionType.BUTTON && parent == null)
      throw new IllegalArgumentException("按钮必须隶属菜单");
    if (parent != null
        && (parent.getType() != PermissionType.MENU
            || !parent.getSystem().getId().equals(p.getSystem().getId())))
      throw new IllegalArgumentException("父节点必须是同一应用的菜单");
    for (Permission next = parent; next != null; next = next.getParent())
      if (Objects.equals(next.getId(), p.getId())) throw new IllegalArgumentException("权限树不能形成循环");
    if (type == PermissionType.BUTTON
        && p.getId() != null
        && permissions.existsByParent_Id(p.getId())) throw new ConflictException("有子节点的菜单不能改为按钮");
    if (type == PermissionType.BUTTON && path != null && !path.isBlank())
      throw new IllegalArgumentException("按钮不设置菜单地址");
    p.setName(name);
    p.setType(type);
    p.setMenuPath(path == null || path.isBlank() ? null : path);
    p.setParent(parent);
  }

  @Transactional
  public PermissionDtos.View create(PermissionDtos.Input input) {
    if (permissions.existsByCode(input.code())) throw new ConflictException("前端权限编码已存在");
    var p = new Permission();
    p.setCode(input.code());
    p.setSystem(systems.require(input.systemId()));
    apply(p, input.name(), input.type(), input.menuPath(), input.parentId());
    return view(permissions.save(p));
  }

  @Transactional
  public PermissionDtos.View update(Long id, PermissionDtos.Update input) {
    var p = require(id);
    apply(p, input.name(), input.type(), input.menuPath(), input.parentId());
    return view(p);
  }

  @Transactional
  public PermissionDtos.View status(Long id, boolean active) {
    var p = require(id);
    p.setActive(active);
    return view(p);
  }

  @Transactional
  public void delete(Long id) {
    var p = require(id);
    if (permissions.existsByParent_Id(id)
        || grants.existsByPermission_Id(id)
        || bindings.existsByPermission_Id(id)) throw new ConflictException("仍有子节点、角色授权或 API 绑定");
    permissions.delete(p);
  }

  @Transactional
  public void bind(Long permissionId, Long resourceId) {
    var permission = require(permissionId);
    var resource = resources.require(resourceId);
    if (!permission.getSystem().getId().equals(resource.getSystem().getId()))
      throw new IllegalArgumentException("前端权限与 API 资源必须属于同一应用");
    if (!bindings.existsByPermission_IdAndResource_Id(permissionId, resourceId))
      bindings.save(new PermissionApi(permission, resource));
  }

  @Transactional
  public void unbind(Long permissionId, Long resourceId) {
    require(permissionId);
    resources.require(resourceId);
    bindings.deleteByPermission_IdAndResource_Id(permissionId, resourceId);
  }

  public List<ResourceDtos.View> resources(Long permissionId) {
    require(permissionId);
    return bindings.findAllByPermission_Id(permissionId).stream()
        .map(b -> ApiResourceService.view(b.getResource()))
        .toList();
  }

  public static boolean usable(Permission p) {
    for (Permission n = p; n != null; n = n.getParent())
      if (!n.getActive() || !n.getSystem().getEnabled()) return false;
    return true;
  }

  public static List<PermissionDtos.Tree> tree(Collection<Permission> granted) {
    Map<Long, Permission> nodes = new LinkedHashMap<>();
    Set<Long> exact = new HashSet<>();
    for (Permission p : granted) {
      exact.add(p.getId());
      for (Permission n = p; n != null; n = n.getParent()) nodes.putIfAbsent(n.getId(), n);
    }
    return nodes.values().stream()
        .filter(p -> p.getParent() == null)
        .map(p -> branch(p, nodes.values(), exact))
        .toList();
  }

  private static PermissionDtos.Tree branch(
      Permission p, Collection<Permission> nodes, Set<Long> exact) {
    return new PermissionDtos.Tree(
        view(p),
        exact.contains(p.getId()),
        nodes.stream()
            .filter(n -> n.getParent() != null && n.getParent().getId().equals(p.getId()))
            .map(n -> branch(n, nodes, exact))
            .toList());
  }

  public List<PermissionDtos.Tree> tree(Long systemId) {
    return tree(
        (systemId == null
            ? permissions.findAll()
            : permissions.findAllBySystem_IdOrderById(systemId)));
  }
}
