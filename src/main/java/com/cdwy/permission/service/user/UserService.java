package com.cdwy.permission.service.user;

import cn.edu.xmu.clonefactory.util.CloneFactory;
import com.cdwy.permission.config.AccessPolicy;
import com.cdwy.permission.dto.common.CommonDtos.PageResult;
import com.cdwy.permission.dto.user.*;
import com.cdwy.permission.entity.user.*;
import com.cdwy.permission.exception.*;
import com.cdwy.permission.repository.organization.DepartmentRepository;
import com.cdwy.permission.repository.rbac.UserRoleRepository;
import com.cdwy.permission.repository.user.UserAccountRepository;
import java.time.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户资料、人员身份属性、凭证及账号状态；不管理角色授权。 */
@Service
@Transactional(readOnly = true)
public class UserService {
  private final UserAccountRepository users;
  private final DepartmentRepository departments;
  private final UserRoleRepository grants;
  private final PasswordEncoder encoder;
  private final AccessPolicy policy;

  public UserService(
      UserAccountRepository users,
      DepartmentRepository departments,
      UserRoleRepository grants,
      PasswordEncoder encoder,
      AccessPolicy policy) {
    this.users = users;
    this.departments = departments;
    this.grants = grants;
    this.encoder = encoder;
    this.policy = policy;
  }

  public UserAccount require(Long id) {
    return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
  }

  private UserAccount lock(Long id) {
    return users.lockById(id).orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
  }

  public UserView get(Long id) {
    return UserViews.view(require(id));
  }

  public PageResult<UserView> list(int page, int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("分页参数不合法，size 为 1 到 100");
    var result = users.findAll(PageRequest.of(page, size, Sort.by("id")));
    return new PageResult<>(
        result.map(UserViews::view).getContent(), result.getTotalElements(), page, size);
  }

  @Transactional
  public UserView create(CreateUserRequest input) {
    if (users.existsByUsername(input.getUsername())) throw new ConflictException("登录名已存在");
    PasswordPolicy.validate(input.getPassword());
    UserAccount user = CloneFactory.copy(new UserAccount(), input);
    user.setPasswordHash(encoder.encode(input.getPassword()));
    attributes(user, input.getDepartmentId(), input.getPersonType(), input.getExpiresOn());
    return UserViews.view(users.save(user));
  }

  @Transactional
  public UserView update(Long id, UserDtos.Update input) {
    UserAccount user = lock(id);
    if (user.getAdministrator()
        && (input.personType() != PersonType.EMPLOYEE || input.expiresOn() != null))
      throw new ForbiddenException("管理员不能改为有期限的人员身份");
    user.setDisplayName(input.displayName());
    attributes(user, input.departmentId(), input.personType(), input.expiresOn());
    return UserViews.view(user);
  }

  private void attributes(UserAccount user, Long departmentId, PersonType type, LocalDate day) {
    user.setDepartment(
        departmentId == null
            ? null
            : departments
                .findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("部门不存在")));
    if (type == PersonType.EMPLOYEE) {
      if (day != null) throw new IllegalArgumentException("正式员工不设置临时有效期");
      user.setExpiresAt(null);
    } else {
      if (day == null) throw new IllegalArgumentException("外部或临时人员必须设置有效期");
      LocalDate today = LocalDate.now(policy.zoneId());
      int maximum =
          type == PersonType.EXTERNAL ? policy.externalMaxDays() : policy.temporaryMaxDays();
      if (day.isBefore(today) || day.isAfter(today.plusDays(maximum)))
        throw new IllegalArgumentException("有效期须在今天至 " + maximum + " 天以内");
      user.setExpiresAt(day.plusDays(1).atStartOfDay(policy.zoneId()).toInstant());
    }
    user.setPersonType(type);
  }

  @Transactional
  public UserView status(Long actor, Long id, boolean enabled) {
    UserAccount user = lock(id);
    if (!enabled && (actor.equals(id) || user.getAdministrator()))
      throw new ForbiddenException("不能停用本人或管理员");
    if (user.getEnabled() != enabled) {
      user.setEnabled(enabled);
      user.setCredentialVersion(user.getCredentialVersion() + 1);
    }
    return UserViews.view(user);
  }

  @Transactional
  public void password(Long id, String password) {
    PasswordPolicy.validate(password);
    UserAccount user = lock(id);
    user.setPasswordHash(encoder.encode(password));
    user.setCredentialVersion(user.getCredentialVersion() + 1);
  }

  @Transactional
  public void delete(Long actor, Long id) {
    UserAccount user = lock(id);
    if (actor.equals(id) || user.getAdministrator()) throw new ForbiddenException("不能删除本人或管理员");
    grants.deleteAllByUser_Id(id);
    users.delete(user);
  }
}
