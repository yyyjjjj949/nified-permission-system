package com.cdwy.permission.repository.role;

import com.cdwy.permission.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  boolean existsByCode(String code);
}
