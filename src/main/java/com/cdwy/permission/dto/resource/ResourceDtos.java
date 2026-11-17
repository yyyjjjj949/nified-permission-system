package com.cdwy.permission.dto.resource;

import jakarta.validation.constraints.*;

public final class ResourceDtos {
  private ResourceDtos() {}

  public record Create(
      @NotNull @Positive Long systemId,
      @NotBlank @Size(max = 128) String name,
      @NotBlank @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE") String method,
      @NotBlank @Size(max = 255) String path) {}

  public record Update(@NotBlank @Size(max = 128) String name) {}

  public record View(
      Long id, Long systemId, String name, String method, String path, boolean active) {}
}
