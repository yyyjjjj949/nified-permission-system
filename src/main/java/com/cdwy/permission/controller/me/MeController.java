package com.cdwy.permission.controller.me;

import com.cdwy.permission.config.CurrentUser;
import com.cdwy.permission.dto.auth.AuthDtos;
import com.cdwy.permission.dto.permission.PermissionDtos;
import com.cdwy.permission.dto.user.UserView;
import com.cdwy.permission.service.auth.AuthService;
import com.cdwy.permission.service.rbac.AuthorizationService;
import com.cdwy.permission.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {
  private final UserService users;
  private final AuthService auth;
  private final AuthorizationService authorization;

  public MeController(UserService users, AuthService auth, AuthorizationService authorization) {
    this.users = users;
    this.auth = auth;
    this.authorization = authorization;
  }

  @GetMapping
  public UserView me(HttpServletRequest request) {
    return users.get(CurrentUser.from(request).id());
  }

  @GetMapping("/permissions")
  public List<PermissionDtos.View> permissions(HttpServletRequest request) {
    return authorization.effectiveViews(CurrentUser.from(request).id());
  }

  @GetMapping("/menu")
  public List<PermissionDtos.Tree> menu(HttpServletRequest request) {
    return authorization.menu(CurrentUser.from(request).id());
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void password(
      HttpServletRequest request, @Valid @RequestBody AuthDtos.PasswordChange input) {
    auth.changePassword(CurrentUser.from(request).id(), input);
  }
}
