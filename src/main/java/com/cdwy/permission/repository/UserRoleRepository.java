package com.cdwy.permission.repository;

import com.cdwy.permission.entity.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    List<UserRole> findAllByUser_Id(Long userId);

    List<UserRole> findAllByRole_Id(Long roleId);

    void deleteByUser_IdAndRole_Id(Long userId, Long roleId);

    void deleteAllByUser_Id(Long userId);

    void deleteAllByRole_Id(Long roleId);
}
