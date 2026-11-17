package com.cdwy.permission.dto.role;

import jakarta.validation.constraints.*;

public final class RoleDtos {
  private RoleDtos() {}

  public record Create(
      @NotBlank @Pattern(regexp = "[a-z][a-z0-9_-]{0,63}") String code,
      @NotBlank @Size(max = 128) String name,
      @Size(max = 255) String description) {}

  public record Update(
      @NotBlank @Size(max = 128) String name, @Size(max = 255) String description) {}

  public record View(Long id, String code, String name, String description, boolean enabled) {}
}
