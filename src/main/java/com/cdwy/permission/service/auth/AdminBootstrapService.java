package com.cdwy.permission.service.auth;

import com.cdwy.permission.entity.user.UserAccount;
import com.cdwy.permission.repository.user.UserAccountRepository;
import com.cdwy.permission.service.user.PasswordPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 首次管理员是部署启动配置，不提供初始化 HTTP API。 */
@Service
public class AdminBootstrapService {
  private final UserAccountRepository users;
  private final PasswordEncoder encoder;

  public AdminBootstrapService(UserAccountRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional
  public void initialize(String username, String password) {
    if (username.isBlank() && password.isBlank()) return;
    if (!username.matches("[A-Za-z0-9_.-]{1,64}"))
      throw new IllegalArgumentException("管理员登录名格式不合法");
    PasswordPolicy.validate(password);
    var existing = users.findByUsername(username);
    if (existing.isPresent()) {
      if (!existing.get().getAdministrator()) throw new IllegalStateException("禁止将普通账号自动提升为管理员");
      return;
    }
    if (users.existsByAdministratorTrue()) throw new IllegalStateException("已存在首次管理员");
    var user = new UserAccount();
    user.setUsername(username);
    user.setDisplayName("系统管理员");
    user.setAdministrator(true);
    user.setPasswordHash(encoder.encode(password));
    users.save(user);
  }
}
