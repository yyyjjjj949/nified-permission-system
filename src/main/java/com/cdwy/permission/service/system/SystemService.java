package com.cdwy.permission.service.system;

import com.cdwy.permission.dto.system.SystemDtos;
import com.cdwy.permission.entity.system.BusinessSystem;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.system.BusinessSystemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SystemService {
  private final BusinessSystemRepository repository;
  private final com.cdwy.permission.repository.permission.PermissionRepository permissions;
  private final com.cdwy.permission.repository.resource.ApiResourceRepository resources;

  public SystemService(
      BusinessSystemRepository repository,
      com.cdwy.permission.repository.permission.PermissionRepository permissions,
      com.cdwy.permission.repository.resource.ApiResourceRepository resources) {
    this.repository = repository;
    this.permissions = permissions;
    this.resources = resources;
  }

  public BusinessSystem require(Long id) {
    return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("对象不存在"));
  }

  public static SystemDtos.View view(BusinessSystem value) {
    return new SystemDtos.View(
        value.getId(), value.getCode(), value.getName(), value.getBaseUrl(), value.getEnabled());
  }

  public List<SystemDtos.View> list() {
    return repository.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(SystemService::view)
        .toList();
  }

  @Transactional
  public SystemDtos.View create(SystemDtos.Create input) {
    if (repository.existsByCode(input.code())) throw new ConflictException("编码已存在");
    var value = new BusinessSystem();
    value.setCode(input.code());
    value.setName(input.name());
    value.setBaseUrl(input.baseUrl());
    return view(repository.save(value));
  }

  @Transactional
  public SystemDtos.View update(Long id, SystemDtos.Update input) {
    var value = require(id);
    value.setName(input.name());
    value.setBaseUrl(input.baseUrl());
    return view(value);
  }

  @Transactional
  public SystemDtos.View status(Long id, boolean enabled) {
    var value = require(id);
    value.setEnabled(enabled);
    return view(value);
  }

  @Transactional
  public void delete(Long id) {
    var value = require(id);
    if (permissions.existsBySystem_Id(id) || resources.existsBySystem_Id(id))
      throw new ConflictException("仍有授权或资源引用，请先解除关联");
    repository.delete(value);
  }
}
