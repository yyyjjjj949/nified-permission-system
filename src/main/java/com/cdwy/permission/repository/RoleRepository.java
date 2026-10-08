package com.cdwy.permission.repository;

import com.cdwy.permission.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByCode(String code);
}
