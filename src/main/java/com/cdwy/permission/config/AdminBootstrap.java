package com.cdwy.permission.config;

import com.cdwy.permission.service.auth.AdminBootstrapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
  private final AdminBootstrapService service;
  private final String username;
  private final String password;

  public AdminBootstrap(
      AdminBootstrapService service,
      @Value("${permission.bootstrap.username:}") String username,
      @Value("${permission.bootstrap.password:}") String password) {
    this.service = service;
    this.username = username;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    service.initialize(username, password);
  }
}
