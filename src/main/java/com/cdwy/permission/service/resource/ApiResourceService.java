package com.cdwy.permission.service.resource;

import com.cdwy.permission.dto.resource.ResourceDtos;
import com.cdwy.permission.entity.resource.ApiResource;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.rbac.PermissionApiRepository;
import com.cdwy.permission.repository.resource.ApiResourceRepository;
import com.cdwy.permission.service.system.SystemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.pattern.PathPatternParser;

@Service
@Transactional(readOnly = true)
public class ApiResourceService {
  private final ApiResourceRepository resources;
  private final PermissionApiRepository bindings;
  private final SystemService systems;

  public ApiResourceService(
      ApiResourceRepository resources, PermissionApiRepository bindings, SystemService systems) {
    this.resources = resources;
    this.bindings = bindings;
    this.systems = systems;
  }

  public ApiResource require(Long id) {
    return resources.findById(id).orElseThrow(() -> new ResourceNotFoundException("API 资源不存在"));
  }

  public static ResourceDtos.View view(ApiResource r) {
    return new ResourceDtos.View(
        r.getId(), r.getSystem().getId(), r.getName(), r.getMethod(), r.getPath(), r.getActive());
  }

  public List<ResourceDtos.View> list() {
    return resources.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(ApiResourceService::view)
        .toList();
  }

  @Transactional
  public ResourceDtos.View create(ResourceDtos.Create input) {
    if (!input.path().startsWith("/")
        || input.path().contains("?")
        || input.path().contains("#")
        || input.path().contains("**"))
      throw new IllegalArgumentException("资源路径须为不含查询参数的绝对路径，不支持 ** 通配符");
    PathPatternParser.defaultInstance.parse(input.path());
    if (resources.existsBySystem_IdAndMethodAndPath(input.systemId(), input.method(), input.path()))
      throw new ConflictException("API 资源已存在");
    var r = new ApiResource();
    r.setSystem(systems.require(input.systemId()));
    r.setMethod(input.method());
    r.setPath(input.path());
    r.setName(input.name());
    return view(resources.save(r));
  }

  @Transactional
  public ResourceDtos.View update(Long id, ResourceDtos.Update input) {
    var value = require(id);
    value.setName(input.name());
    return view(value);
  }

  @Transactional
  public ResourceDtos.View status(Long id, boolean active) {
    var value = require(id);
    value.setActive(active);
    return view(value);
  }

  @Transactional
  public void delete(Long id) {
    var value = require(id);
    if (bindings.existsByResource_Id(id)) throw new ConflictException("API 资源仍绑定前端权限");
    resources.delete(value);
  }
}
