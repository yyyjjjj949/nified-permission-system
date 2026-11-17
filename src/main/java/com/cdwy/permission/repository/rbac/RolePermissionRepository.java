package com.cdwy.permission.repository.rbac;

import com.cdwy.permission.entity.rbac.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
  java.util.List<RolePermission> findAllByRole_Id(Long id);

  boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

  boolean existsByPermission_Id(Long id);

  boolean existsByRole_Id(Long id);

  void deleteByRole_IdAndPermission_Id(Long roleId, Long permissionId);
}
