package com.cdwy.permission.controller.user;

import com.cdwy.permission.config.CurrentUser;
import com.cdwy.permission.dto.common.CommonDtos.*;
import com.cdwy.permission.dto.user.*;
import com.cdwy.permission.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserView create(@Valid @RequestBody CreateUserRequest input) {
    return service.create(input);
  }

  @GetMapping
  public PageResult<UserView> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    return service.list(page, size);
  }

  @GetMapping("/{id}")
  public UserView get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public UserView update(@PathVariable Long id, @Valid @RequestBody UserDtos.Update input) {
    return service.update(id, input);
  }

  @PutMapping("/{id}/status")
  public UserView status(
      HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody Status input) {
    return service.status(CurrentUser.from(request).id(), id, input.enabled());
  }

  @PutMapping("/{id}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void password(@PathVariable Long id, @Valid @RequestBody UserDtos.Password input) {
    service.password(id, input.password());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(HttpServletRequest request, @PathVariable Long id) {
    service.delete(CurrentUser.from(request).id(), id);
  }
}
