package com.cdwy.permission.repository.permission;

import com.cdwy.permission.entity.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  boolean existsByCode(String code);

  boolean existsByParent_Id(Long parentId);

  boolean existsBySystem_Id(Long systemId);

  java.util.List<Permission> findAllBySystem_IdOrderById(Long systemId);
}
