package com.cdwy.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NifiedPermissionSystemApplication {
  public static void main(String[] args) {
    SpringApplication.run(NifiedPermissionSystemApplication.class, args);
  }
}
