package com.cdwy.permission.repository.organization;

import com.cdwy.permission.entity.organization.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  boolean existsByName(String name);

  boolean existsByParent_Id(Long parentId);
}
