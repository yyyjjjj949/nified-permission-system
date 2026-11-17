package com.cdwy.permission.repository.rbac;

import com.cdwy.permission.entity.rbac.PermissionApi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionApiRepository extends JpaRepository<PermissionApi, Long> {
  java.util.List<PermissionApi> findAllByPermission_Id(Long id);

  java.util.List<PermissionApi> findAllByResource_Id(Long id);

  boolean existsByPermission_IdAndResource_Id(Long permissionId, Long resourceId);

  boolean existsByPermission_Id(Long id);

  boolean existsByResource_Id(Long id);

  void deleteByPermission_IdAndResource_Id(Long permissionId, Long resourceId);
}
