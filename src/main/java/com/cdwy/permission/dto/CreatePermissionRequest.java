package com.cdwy.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotNull Long systemId,
        @NotBlank @Size(max = 128) String code,
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(max = 255) String menuPath
) {
}
