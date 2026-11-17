package com.cdwy.permission.entity.permission;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_permission")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 128)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PermissionType type;

  @Column(length = 255)
  private String menuPath;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Permission parent;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "system_id", nullable = false)
  private com.cdwy.permission.entity.system.BusinessSystem system;

  @Column(nullable = false)
  private boolean active = true;

  public Permission() {}

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String value) {
    code = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public PermissionType getType() {
    return type;
  }

  public void setType(PermissionType value) {
    type = value;
  }

  public String getMenuPath() {
    return menuPath;
  }

  public void setMenuPath(String value) {
    menuPath = value;
  }

  public Permission getParent() {
    return parent;
  }

  public void setParent(Permission value) {
    parent = value;
  }

  public com.cdwy.permission.entity.system.BusinessSystem getSystem() {
    return system;
  }

  public void setSystem(com.cdwy.permission.entity.system.BusinessSystem value) {
    system = value;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    active = value;
  }
}
