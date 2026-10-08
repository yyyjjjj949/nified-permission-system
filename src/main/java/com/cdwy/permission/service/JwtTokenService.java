package com.cdwy.permission.service;

import com.cdwy.permission.exception.AuthenticationFailedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long expirationSeconds;
    private final ConcurrentMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public JwtTokenService(
            @Value("${permission.jwt.secret}") String secret,
            @Value("${permission.jwt.expiration-seconds:7200}") long expirationSeconds) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("permission.jwt.secret 至少需要 32 个字节");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("permission.jwt.expiration-seconds 必须大于 0");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String issue(Long userId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public Long requireUserId(String authorizationHeader) {
        Claims claims = parse(extractToken(authorizationHeader));
        String tokenId = claims.getId();
        Instant revokedUntil = tokenId == null ? null : revokedTokens.get(tokenId);
        if (revokedUntil != null) {
            if (revokedUntil.isAfter(Instant.now())) {
                throw new AuthenticationFailedException();
            }
            revokedTokens.remove(tokenId, revokedUntil);
        }
        try {
            return Long.valueOf(claims.getSubject());
        } catch (RuntimeException exception) {
            throw new AuthenticationFailedException();
        }
    }

    public void revoke(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return;
        }
        try {
            Claims claims = parse(extractToken(authorizationHeader));
            if (claims.getId() != null && claims.getExpiration() != null) {
                revokedTokens.put(claims.getId(), claims.getExpiration().toInstant());
            }
        } catch (AuthenticationFailedException ignored) {
            // Logging out with an expired or malformed token is already complete.
        }
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthenticationFailedException();
        }
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
