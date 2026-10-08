package com.cdwy.permission.repository;

import com.cdwy.permission.entity.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByCode(String code);

    List<Permission> findAllBySystem_Id(Long systemId);
}
