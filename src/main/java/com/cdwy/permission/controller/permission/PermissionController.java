package com.cdwy.permission.controller.permission;

import com.cdwy.permission.dto.common.CommonDtos.Activity;
import com.cdwy.permission.dto.permission.PermissionDtos;
import com.cdwy.permission.dto.resource.ResourceDtos;
import com.cdwy.permission.service.permission.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
  private final PermissionService service;

  public PermissionController(PermissionService service) {
    this.service = service;
  }

  @GetMapping
  public List<PermissionDtos.View> list(@RequestParam(required = false) Long systemId) {
    return service.list(systemId);
  }

  @GetMapping("/tree")
  public List<PermissionDtos.Tree> tree(@RequestParam(required = false) Long systemId) {
    return service.tree(systemId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PermissionDtos.View create(@Valid @RequestBody PermissionDtos.Input input) {
    return service.create(input);
  }

  @PutMapping("/{id}")
  public PermissionDtos.View update(
      @PathVariable Long id, @Valid @RequestBody PermissionDtos.Update input) {
    return service.update(id, input);
  }

  @PutMapping("/{id}/status")
  public PermissionDtos.View status(@PathVariable Long id, @Valid @RequestBody Activity input) {
    return service.status(id, input.active());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @GetMapping("/{id}/resources")
  public List<ResourceDtos.View> resources(@PathVariable Long id) {
    return service.resources(id);
  }

  @PutMapping("/{id}/resources/{resourceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void bind(@PathVariable Long id, @PathVariable Long resourceId) {
    service.bind(id, resourceId);
  }

  @DeleteMapping("/{id}/resources/{resourceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unbind(@PathVariable Long id, @PathVariable Long resourceId) {
    service.unbind(id, resourceId);
  }
}
