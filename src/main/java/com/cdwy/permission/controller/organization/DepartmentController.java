package com.cdwy.permission.controller.organization;

import com.cdwy.permission.dto.organization.DepartmentDtos;
import com.cdwy.permission.service.organization.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
  private final DepartmentService service;

  public DepartmentController(DepartmentService service) {
    this.service = service;
  }

  @GetMapping
  public List<DepartmentDtos.View> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DepartmentDtos.View create(@Valid @RequestBody DepartmentDtos.Input input) {
    return service.create(input);
  }

  @PutMapping("/{id}")
  public DepartmentDtos.View update(
      @PathVariable Long id, @Valid @RequestBody DepartmentDtos.Input input) {
    return service.update(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
