package com.cdwy.permission.repository;

import com.cdwy.permission.entity.BusinessSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSystemRepository extends JpaRepository<BusinessSystem, Long> {

    boolean existsByCode(String code);
}
