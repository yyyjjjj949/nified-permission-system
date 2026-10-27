package com.cdwy.permission.repository;

import com.cdwy.permission.entity.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    List<RolePermission> findAllByRole_Id(Long roleId);

    List<RolePermission> findAllByPermission_Id(Long permissionId);

    void deleteByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    void deleteAllByRole_Id(Long roleId);

    void deleteAllByPermission_Id(Long permissionId);
}
