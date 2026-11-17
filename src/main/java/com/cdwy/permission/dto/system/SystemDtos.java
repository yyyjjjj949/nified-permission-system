package com.cdwy.permission.dto.system;

import jakarta.validation.constraints.*;

public final class SystemDtos {
  private SystemDtos() {}

  public record Create(
      @NotBlank @Pattern(regexp = "[a-z][a-z0-9_-]{0,63}") String code,
      @NotBlank @Size(max = 128) String name,
      @Size(max = 255) String baseUrl) {}

  public record Update(@NotBlank @Size(max = 128) String name, @Size(max = 255) String baseUrl) {}

  public record View(Long id, String code, String name, String baseUrl, boolean enabled) {}
}
