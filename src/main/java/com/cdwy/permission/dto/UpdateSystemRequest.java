package com.cdwy.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSystemRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 255) String baseUrl,
        @NotNull Boolean enabled
) {
}
