package com.cdwy.permission.dto.user;

@cn.edu.xmu.clonefactory.CopyFrom(com.cdwy.permission.entity.user.UserAccount.class)
public class UserView {

  private Long id;

  private String username;

  private String displayName;

  private boolean enabled;

  private boolean administrator;

  private Long departmentId;

  private com.cdwy.permission.entity.user.PersonType personType;

  private java.time.Instant expiresAt;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    id = value;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    username = value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String value) {
    displayName = value;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean value) {
    enabled = value;
  }

  public boolean getAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean value) {
    administrator = value;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Long value) {
    departmentId = value;
  }

  public com.cdwy.permission.entity.user.PersonType getPersonType() {
    return personType;
  }

  public void setPersonType(com.cdwy.permission.entity.user.PersonType value) {
    personType = value;
  }

  public java.time.Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(java.time.Instant value) {
    expiresAt = value;
  }
}
