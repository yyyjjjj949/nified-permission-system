package com.cdwy.permission.controller.auth;

import com.cdwy.permission.dto.auth.AuthDtos;
import com.cdwy.permission.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public AuthDtos.Token login(@Valid @RequestBody AuthDtos.Login input) {
    return service.login(input);
  }
}
