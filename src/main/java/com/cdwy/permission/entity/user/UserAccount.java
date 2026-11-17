package com.cdwy.permission.entity.user;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_user")
@SecondaryTable(
    name = "sys_account",
    pkJoinColumns = @PrimaryKeyJoinColumn(name = "user_id"),
    uniqueConstraints = @UniqueConstraint(name = "uk_account_username", columnNames = "username"))
public class UserAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(table = "sys_account", nullable = false, length = 64)
  private String username;

  @Column(table = "sys_account", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 128)
  private String displayName;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false)
  private boolean administrator = false;

  @Column(nullable = false)
  private long credentialVersion = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PersonType personType = PersonType.EMPLOYEE;

  private java.time.Instant expiresAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private com.cdwy.permission.entity.organization.Department department;

  public UserAccount() {}

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    username = value;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String value) {
    passwordHash = value;
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

  public long getCredentialVersion() {
    return credentialVersion;
  }

  public void setCredentialVersion(long value) {
    credentialVersion = value;
  }

  public PersonType getPersonType() {
    return personType;
  }

  public void setPersonType(PersonType value) {
    personType = value;
  }

  public java.time.Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(java.time.Instant value) {
    expiresAt = value;
  }

  public com.cdwy.permission.entity.organization.Department getDepartment() {
    return department;
  }

  public void setDepartment(com.cdwy.permission.entity.organization.Department value) {
    department = value;
  }

  public Long getDepartmentId() {
    return department == null ? null : department.getId();
  }

  public boolean usable(java.time.Instant now) {
    return enabled && (expiresAt == null || expiresAt.isAfter(now));
  }
}
