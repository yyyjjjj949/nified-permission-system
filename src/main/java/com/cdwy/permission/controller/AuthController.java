package com.cdwy.permission.controller;

import com.cdwy.permission.dto.LoginRequest;
import com.cdwy.permission.dto.LoginResponse;
import com.cdwy.permission.service.RbacService;
import com.cdwy.permission.service.SessionTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RbacService rbacService;
    private final SessionTokenService sessionTokenService;

    public AuthController(RbacService rbacService, SessionTokenService sessionTokenService) {
        this.rbacService = rbacService;
        this.sessionTokenService = sessionTokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return rbacService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @org.springframework.web.bind.annotation.RequestHeader(
                    value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        sessionTokenService.revoke(authorization);
        return ResponseEntity.noContent().build();
    }
}
