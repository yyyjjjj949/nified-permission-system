package com.cdwy.permission.dto.organization;

import jakarta.validation.constraints.*;

public final class DepartmentDtos {
  private DepartmentDtos() {}

  public record Input(@NotBlank @Size(max = 128) String name, Long parentId) {}

  public record View(Long id, String name, Long parentId) {}
}
