package com.cdwy.permission.service.auth;

import com.cdwy.permission.config.JwtSettings;
import com.cdwy.permission.entity.user.UserAccount;
import com.cdwy.permission.exception.AuthenticationFailedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** 无登录会话存储。签发与验签只处理 JWT，自身不访问数据库。 */
@Service
public class JwtTokenService {
  private final SecretKey signingKey;
  private final long expirationSeconds;

  public JwtTokenService(JwtSettings settings) {
    byte[] bytes = settings.secret().getBytes(StandardCharsets.UTF_8);
    if (bytes.length > 0 && bytes.length < 32) throw new IllegalArgumentException("JWT 密钥至少 32 字节");
    signingKey = bytes.length == 0 ? Jwts.SIG.HS256.key().build() : Keys.hmacShaKeyFor(bytes);
    expirationSeconds = settings.expirationSeconds();
  }

  public long expiresIn() {
    return expirationSeconds;
  }

  public String issue(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expirationSeconds)))
        .claim("cv", user.getCredentialVersion())
        .signWith(signingKey)
        .compact();
  }

  public record Identity(Long userId, long credentialVersion) {}

  public Identity verify(String header) {
    if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) throw invalid();
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(signingKey)
              .build()
              .parseSignedClaims(header.substring(7).trim())
              .getPayload();
      long id = Long.parseLong(claims.getSubject());
      Number version = claims.get("cv", Number.class);
      if (id < 1
          || version == null
          || claims.getIssuedAt() == null
          || claims.getExpiration() == null
          || claims.getIssuedAt().after(new Date())
          || !claims.getExpiration().after(claims.getIssuedAt())) throw invalid();
      return new Identity(id, version.longValue());
    } catch (JwtException | IllegalArgumentException e) {
      throw invalid();
    }
  }

  private static AuthenticationFailedException invalid() {
    return new AuthenticationFailedException("Token 无效或已过期，请重新登录");
  }
}
