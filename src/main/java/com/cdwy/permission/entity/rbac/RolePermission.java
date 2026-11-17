package com.cdwy.permission.entity.rbac;

import jakarta.persistence.*;

@Entity
@Table(
    name = "sys_role_permission",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_sys_role_permission",
            columnNames = {"role_id", "permission_id"}))
public class RolePermission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private com.cdwy.permission.entity.role.Role role;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "permission_id", nullable = false)
  private com.cdwy.permission.entity.permission.Permission permission;

  public RolePermission() {}

  public RolePermission(
      com.cdwy.permission.entity.role.Role role,
      com.cdwy.permission.entity.permission.Permission permission) {
    this.role = role;
    this.permission = permission;
  }

  public Long getId() {
    return id;
  }

  public com.cdwy.permission.entity.role.Role getRole() {
    return role;
  }

  public com.cdwy.permission.entity.permission.Permission getPermission() {
    return permission;
  }
}
