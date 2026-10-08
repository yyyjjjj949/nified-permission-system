package com.cdwy.permission.dto;

public record PermissionResponse(
        Long id,
        Long systemId,
        String systemCode,
        String code,
        String name,
        String menuPath
) {
}
