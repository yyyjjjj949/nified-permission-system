package com.cdwy.permission.service.user;

import cn.edu.xmu.clonefactory.util.CloneFactory;
import com.cdwy.permission.dto.user.UserView;
import com.cdwy.permission.entity.user.UserAccount;

public final class UserViews {
  private UserViews() {}

  public static UserView view(UserAccount user) {
    return CloneFactory.copy(new UserView(), user);
  }
}
