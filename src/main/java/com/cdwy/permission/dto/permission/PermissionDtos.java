package com.cdwy.permission.dto.permission;

import jakarta.validation.constraints.*;

public final class PermissionDtos {
  private PermissionDtos() {}

  public record Input(
      @NotNull @Positive Long systemId,
      @NotBlank @Size(max = 128) String code,
      @NotBlank @Size(max = 128) String name,
      @NotNull com.cdwy.permission.entity.permission.PermissionType type,
      @Size(max = 255) String menuPath,
      Long parentId) {}

  public record Update(
      @NotBlank @Size(max = 128) String name,
      @NotNull com.cdwy.permission.entity.permission.PermissionType type,
      @Size(max = 255) String menuPath,
      Long parentId) {}

  public record View(
      Long id,
      Long systemId,
      String code,
      String name,
      String type,
      String menuPath,
      Long parentId,
      boolean active) {}

  public record Tree(View permission, boolean granted, java.util.List<Tree> children) {}
}
