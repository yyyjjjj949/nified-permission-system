package com.cdwy.permission.controller.resource;

import com.cdwy.permission.dto.common.CommonDtos.Activity;
import com.cdwy.permission.dto.resource.ResourceDtos;
import com.cdwy.permission.service.resource.ApiResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ApiResourceController {
  private final ApiResourceService service;

  public ApiResourceController(ApiResourceService service) {
    this.service = service;
  }

  @GetMapping
  public List<ResourceDtos.View> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResourceDtos.View create(@Valid @RequestBody ResourceDtos.Create input) {
    return service.create(input);
  }

  @PutMapping("/{id}")
  public ResourceDtos.View update(
      @PathVariable Long id, @Valid @RequestBody ResourceDtos.Update input) {
    return service.update(id, input);
  }

  @PutMapping("/{id}/status")
  public ResourceDtos.View status(@PathVariable Long id, @Valid @RequestBody Activity input) {
    return service.status(id, input.active());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
