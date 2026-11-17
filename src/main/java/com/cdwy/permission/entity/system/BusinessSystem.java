package com.cdwy.permission.entity.system;

import jakarta.persistence.*;

@Entity
@Table(name = "biz_system")
public class BusinessSystem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(length = 255)
  private String baseUrl;

  @Column(nullable = false)
  private boolean enabled = true;

  public BusinessSystem() {}

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

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String value) {
    baseUrl = value;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean value) {
    enabled = value;
  }
}
