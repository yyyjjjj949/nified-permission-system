package com.cdwy.permission.controller.system;

import com.cdwy.permission.dto.common.CommonDtos.Status;
import com.cdwy.permission.dto.system.SystemDtos;
import com.cdwy.permission.service.system.SystemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/systems")
public class SystemController {
  private final SystemService service;

  public SystemController(SystemService service) {
    this.service = service;
  }

  @GetMapping
  public List<SystemDtos.View> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SystemDtos.View create(@Valid @RequestBody SystemDtos.Create input) {
    return service.create(input);
  }

  @PutMapping("/{id}")
  public SystemDtos.View update(
      @PathVariable Long id, @Valid @RequestBody SystemDtos.Update input) {
    return service.update(id, input);
  }

  @PutMapping("/{id}/status")
  public SystemDtos.View status(@PathVariable Long id, @Valid @RequestBody Status input) {
    return service.status(id, input.enabled());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
