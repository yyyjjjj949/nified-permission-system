package com.cdwy.permission.repository.user;

import com.cdwy.permission.entity.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  java.util.Optional<UserAccount> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByAdministratorTrue();

  boolean existsByDepartment_Id(Long departmentId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select u from UserAccount u where u.id=:id")
  java.util.Optional<UserAccount> lockById(
      @org.springframework.data.repository.query.Param("id") Long id);
}
