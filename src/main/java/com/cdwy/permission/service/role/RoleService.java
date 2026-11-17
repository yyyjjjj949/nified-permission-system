package com.cdwy.permission.service.role;

import com.cdwy.permission.dto.role.RoleDtos;
import com.cdwy.permission.entity.role.Role;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.role.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoleService {
  private final RoleRepository repository;
  private final com.cdwy.permission.repository.rbac.UserRoleRepository userRoles;
  private final com.cdwy.permission.repository.rbac.RolePermissionRepository rolePermissions;

  public RoleService(
      RoleRepository repository,
      com.cdwy.permission.repository.rbac.UserRoleRepository userRoles,
      com.cdwy.permission.repository.rbac.RolePermissionRepository rolePermissions) {
    this.repository = repository;
    this.userRoles = userRoles;
    this.rolePermissions = rolePermissions;
  }

  public Role require(Long id) {
    return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("对象不存在"));
  }

  public static RoleDtos.View view(Role value) {
    return new RoleDtos.View(
        value.getId(),
        value.getCode(),
        value.getName(),
        value.getDescription(),
        value.getEnabled());
  }

  public List<RoleDtos.View> list() {
    return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(RoleService::view)
        .toList();
  }

  @Transactional
  public RoleDtos.View create(RoleDtos.Create input) {
    if (repository.existsByCode(input.code())) throw new ConflictException("编码已存在");
    var value = new Role();
    value.setCode(input.code());
    value.setName(input.name());
    value.setDescription(input.description());
    return view(repository.save(value));
  }

  @Transactional
  public RoleDtos.View update(Long id, RoleDtos.Update input) {
    var value = require(id);
    value.setName(input.name());
    value.setDescription(input.description());
    return view(value);
  }

  @Transactional
  public RoleDtos.View status(Long id, boolean enabled) {
    var value = require(id);
    value.setEnabled(enabled);
    return view(value);
  }

  @Transactional
  public void delete(Long id) {
    var value = require(id);
    if (userRoles.existsByRole_Id(id) || rolePermissions.existsByRole_Id(id))
      throw new ConflictException("仍有授权或资源引用，请先解除关联");
    repository.delete(value);
  }
}
