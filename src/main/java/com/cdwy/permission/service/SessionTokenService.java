package com.cdwy.permission.service;

import com.cdwy.permission.exception.AuthenticationFailedException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

/**
 * Minimal in-memory token service for the current single-node demo.
 * Production deployment should replace this store with a shared token service.
 */
@Service
public class SessionTokenService {

    private final ConcurrentMap<String, Long> sessions = new ConcurrentHashMap<>();

    public String issue(Long userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        return token;
    }

    public Long requireUserId(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        Long userId = sessions.get(token);
        if (userId == null) {
            throw new AuthenticationFailedException();
        }
        return userId;
    }

    public void revoke(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return;
        }
        sessions.remove(extractToken(authorizationHeader));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationFailedException();
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new AuthenticationFailedException();
        }
        return token;
    }
}
