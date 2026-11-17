package com.cdwy.permission.dto.user;

import com.cdwy.permission.entity.user.PersonType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class UserDtos {
  private UserDtos() {}

  public record Update(
      @NotBlank @Size(max = 128) String displayName,
      Long departmentId,
      @NotNull PersonType personType,
      LocalDate expiresOn) {}

  public record Password(@NotBlank String password) {}
}
