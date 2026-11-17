package com.cdwy.permission.repository.rbac;

import com.cdwy.permission.entity.rbac.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
  java.util.List<UserRole> findAllByUser_Id(Long id);

  boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

  boolean existsByRole_Id(Long id);

  void deleteByUser_IdAndRole_Id(Long userId, Long roleId);

  void deleteAllByUser_Id(Long id);
}
