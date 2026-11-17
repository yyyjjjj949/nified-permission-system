package com.cdwy.permission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("permission.jwt")
public record JwtSettings(String secret, long expirationSeconds) {
  public JwtSettings {
    if (expirationSeconds < 1) throw new IllegalArgumentException("Token 有效期必须大于零");
  }
}
