package com.cdwy.permission.entity.rbac;

import jakarta.persistence.*;

@Entity
@Table(
    name = "permission_api",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_permission_api",
            columnNames = {"permission_id", "resource_id"}))
public class PermissionApi {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "permission_id", nullable = false)
  private com.cdwy.permission.entity.permission.Permission permission;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "resource_id", nullable = false)
  private com.cdwy.permission.entity.resource.ApiResource resource;

  public PermissionApi() {}

  public PermissionApi(
      com.cdwy.permission.entity.permission.Permission permission,
      com.cdwy.permission.entity.resource.ApiResource resource) {
    this.permission = permission;
    this.resource = resource;
  }

  public Long getId() {
    return id;
  }

  public com.cdwy.permission.entity.permission.Permission getPermission() {
    return permission;
  }

  public com.cdwy.permission.entity.resource.ApiResource getResource() {
    return resource;
  }
}
