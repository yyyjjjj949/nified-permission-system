package com.cdwy.permission.entity.rbac;

import jakarta.persistence.*;

@Entity
@Table(
    name = "sys_user_role",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_sys_user_role",
            columnNames = {"user_id", "role_id"}))
public class UserRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private com.cdwy.permission.entity.user.UserAccount user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private com.cdwy.permission.entity.role.Role role;

  public UserRole() {}

  public UserRole(
      com.cdwy.permission.entity.user.UserAccount user, com.cdwy.permission.entity.role.Role role) {
    this.user = user;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public com.cdwy.permission.entity.user.UserAccount getUser() {
    return user;
  }

  public com.cdwy.permission.entity.role.Role getRole() {
    return role;
  }
}
