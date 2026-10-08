package com.cdwy.permission.dto;

import cn.edu.xmu.clonefactory.CopyTo;
import com.cdwy.permission.entity.UserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@CopyTo(UserAccount.class)
public class CreateUserRequest {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(max = 128)
    private String displayName;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public String username() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String displayName() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
