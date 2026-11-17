package com.cdwy.permission.repository.system;

import com.cdwy.permission.entity.system.BusinessSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSystemRepository extends JpaRepository<BusinessSystem, Long> {
  boolean existsByCode(String code);
}
