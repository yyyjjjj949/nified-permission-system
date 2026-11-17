package com.cdwy.permission.controller.role;

import com.cdwy.permission.dto.common.CommonDtos.Status;
import com.cdwy.permission.dto.role.RoleDtos;
import com.cdwy.permission.service.role.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
  private final RoleService service;

  public RoleController(RoleService service) {
    this.service = service;
  }

  @GetMapping
  public List<RoleDtos.View> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoleDtos.View create(@Valid @RequestBody RoleDtos.Create input) {
    return service.create(input);
  }

  @PutMapping("/{id}")
  public RoleDtos.View update(@PathVariable Long id, @Valid @RequestBody RoleDtos.Update input) {
    return service.update(id, input);
  }

  @PutMapping("/{id}/status")
  public RoleDtos.View status(@PathVariable Long id, @Valid @RequestBody Status input) {
    return service.status(id, input.enabled());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
