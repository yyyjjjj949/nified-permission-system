package com.cdwy.permission.controller;

import com.cdwy.permission.service.RbacService;
import com.cdwy.permission.service.SessionTokenService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/expenses")
public class DemoExpenseController {

    private final RbacService rbacService;
    private final SessionTokenService sessionTokenService;

    public DemoExpenseController(RbacService rbacService, SessionTokenService sessionTokenService) {
        this.rbacService = rbacService;
        this.sessionTokenService = sessionTokenService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        Long userId = sessionTokenService.requireUserId(authorization);
        rbacService.assertPermission(userId, "expense:list");
        return Map.of(
                "userId", userId,
                "items", java.util.List.of(
                        Map.of("id", 1, "title", "差旅报销", "amount", 1200),
                        Map.of("id", 2, "title", "办公用品报销", "amount", 260)));
    }
}
