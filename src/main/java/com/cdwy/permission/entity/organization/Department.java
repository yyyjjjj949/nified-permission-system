package com.cdwy.permission.entity.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_department")
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 128)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Department parent;

  public Department() {}

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public Department getParent() {
    return parent;
  }

  public void setParent(Department value) {
    parent = value;
  }
}
