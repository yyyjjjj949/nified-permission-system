package com.cdwy.permission.entity.resource;

import jakarta.persistence.*;

@Entity
@Table(
    name = "api_resource",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_api_route",
            columnNames = {"system_id", "method", "path"}))
public class ApiResource {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 8)
  private String method;

  @Column(nullable = false, length = 255)
  private String path;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "system_id", nullable = false)
  private com.cdwy.permission.entity.system.BusinessSystem system;

  @Column(nullable = false)
  private boolean active = true;

  public ApiResource() {}

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String value) {
    method = value;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String value) {
    path = value;
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
