package com.cdwy.permission.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        Long userId,
        String username,
        String displayName,
        List<PermissionResponse> permissions
) {
}
