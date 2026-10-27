package com.cdwy.permission.dto;

public record PermissionCheckResponse(Long userId, String permissionCode, boolean allowed) {
}
