package com.cdwy.permission.config;

import java.time.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("permission.policy")
public record AccessPolicy(int externalMaxDays, int temporaryMaxDays, String zone) {
  public AccessPolicy {
    if (externalMaxDays < 1 || temporaryMaxDays < 1)
      throw new IllegalArgumentException("人员有效期上限必须为正数");
    ZoneId.of(zone);
  }

  public ZoneId zoneId() {
    return ZoneId.of(zone);
  }
}
