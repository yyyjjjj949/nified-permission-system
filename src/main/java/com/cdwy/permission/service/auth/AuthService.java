package com.cdwy.permission.service.auth;

import com.cdwy.permission.dto.auth.AuthDtos;
import com.cdwy.permission.entity.user.UserAccount;
import com.cdwy.permission.exception.AuthenticationFailedException;
import com.cdwy.permission.repository.user.UserAccountRepository;
import com.cdwy.permission.service.user.*;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
  private final UserAccountRepository users;
  private final PasswordEncoder encoder;
  private final JwtTokenService tokens;
  private final UserService userService;

  public AuthService(
      UserAccountRepository users,
      PasswordEncoder encoder,
      JwtTokenService tokens,
      UserService userService) {
    this.users = users;
    this.encoder = encoder;
    this.tokens = tokens;
    this.userService = userService;
  }

  public AuthDtos.Token login(AuthDtos.Login input) {
    UserAccount user =
        users
            .findByUsername(input.username())
            .filter(u -> u.usable(Instant.now()))
            .filter(u -> encoder.matches(input.password(), u.getPasswordHash()))
            .orElseThrow(() -> new AuthenticationFailedException("账号或密码错误，或账号已失效"));
    return new AuthDtos.Token(
        tokens.issue(user), "Bearer", tokens.expiresIn(), UserViews.view(user));
  }

  public UserAccount resolve(String header) {
    var identity = tokens.verify(header);
    return users
        .findById(identity.userId())
        .filter(u -> u.usable(Instant.now()))
        .filter(u -> u.getCredentialVersion() == identity.credentialVersion())
        .orElseThrow(() -> new AuthenticationFailedException("账号或凭证已失效，请重新登录"));
  }

  @Transactional
  public void changePassword(Long id, AuthDtos.PasswordChange input) {
    var user = users.lockById(id).orElseThrow(() -> new AuthenticationFailedException("账号不存在"));
    if (!encoder.matches(input.currentPassword(), user.getPasswordHash()))
      throw new AuthenticationFailedException("原密码错误");
    userService.password(id, input.newPassword());
  }
}
