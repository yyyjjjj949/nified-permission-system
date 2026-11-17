package com.cdwy.permission.config;

import jakarta.servlet.http.HttpServletRequest;

public record CurrentUser(Long id, boolean administrator) {
  public static CurrentUser from(HttpServletRequest request) {
    return (CurrentUser) request.getAttribute(CurrentUser.class.getName());
  }
}
