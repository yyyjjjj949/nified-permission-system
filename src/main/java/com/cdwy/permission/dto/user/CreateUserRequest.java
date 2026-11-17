package com.cdwy.permission.dto.user;

@cn.edu.xmu.clonefactory.CopyTo(com.cdwy.permission.entity.user.UserAccount.class)
public class CreateUserRequest {
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Pattern(regexp = "[A-Za-z0-9_.-]{1,64}")
  private String username;

  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 128)
  private String displayName;

  @jakarta.validation.constraints.NotBlank private String password;

  private Long departmentId;

  @jakarta.validation.constraints.NotNull
  private com.cdwy.permission.entity.user.PersonType personType =
      com.cdwy.permission.entity.user.PersonType.EMPLOYEE;

  private java.time.LocalDate expiresOn;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    password = value;
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

  public java.time.LocalDate getExpiresOn() {
    return expiresOn;
  }

  public void setExpiresOn(java.time.LocalDate value) {
    expiresOn = value;
  }
}
