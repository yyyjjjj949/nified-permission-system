package com.cdwy.permission.dto.auth;

import jakarta.validation.constraints.*;

public final class AuthDtos {
  private AuthDtos() {}

  public record Login(@NotBlank String username, @NotBlank String password) {}

  public record Token(
      String accessToken,
      String tokenType,
      long expiresIn,
      com.cdwy.permission.dto.user.UserView user) {}

  public record PasswordChange(@NotBlank String currentPassword, @NotBlank String newPassword) {}
}
