package com.cdwy.permission.service.organization;

import com.cdwy.permission.dto.organization.DepartmentDtos;
import com.cdwy.permission.entity.organization.Department;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.organization.DepartmentRepository;
import com.cdwy.permission.repository.user.UserAccountRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentService {
  private final DepartmentRepository departments;
  private final UserAccountRepository users;

  public DepartmentService(DepartmentRepository departments, UserAccountRepository users) {
    this.departments = departments;
    this.users = users;
  }

  private Department require(Long id) {
    return departments.findById(id).orElseThrow(() -> new ResourceNotFoundException("部门不存在"));
  }

  public static DepartmentDtos.View view(Department d) {
    return new DepartmentDtos.View(
        d.getId(), d.getName(), d.getParent() == null ? null : d.getParent().getId());
  }

  public List<DepartmentDtos.View> list() {
    return departments.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(DepartmentService::view)
        .toList();
  }

  private void apply(Department value, DepartmentDtos.Input input) {
    if (departments.existsByName(input.name()) && !input.name().equals(value.getName()))
      throw new ConflictException("部门名称已存在");
    Department parent = input.parentId() == null ? null : require(input.parentId());
    for (Department p = parent; p != null; p = p.getParent())
      if (Objects.equals(p.getId(), value.getId())) throw new IllegalArgumentException("部门树不能形成循环");
    value.setName(input.name());
    value.setParent(parent);
  }

  @Transactional
  public DepartmentDtos.View create(DepartmentDtos.Input input) {
    var value = new Department();
    apply(value, input);
    return view(departments.save(value));
  }

  @Transactional
  public DepartmentDtos.View update(Long id, DepartmentDtos.Input input) {
    var value = require(id);
    apply(value, input);
    return view(value);
  }

  @Transactional
  public void delete(Long id) {
    var value = require(id);
    if (departments.existsByParent_Id(id) || users.existsByDepartment_Id(id))
      throw new ConflictException("部门仍有子部门或员工");
    departments.delete(value);
  }
}
