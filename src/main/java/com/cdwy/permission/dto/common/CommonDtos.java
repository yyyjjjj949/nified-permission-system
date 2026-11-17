package com.cdwy.permission.dto.common;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class CommonDtos {
  private CommonDtos() {}

  public record Status(@NotNull Boolean enabled) {}

  public record Activity(@NotNull Boolean active) {}

  public record PageResult<T>(List<T> items, long total, int page, int size) {}
}
