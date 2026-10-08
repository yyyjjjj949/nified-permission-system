package com.cdwy.permission.dto;

import cn.edu.xmu.clonefactory.CopyNotNullTo;
import com.cdwy.permission.entity.UserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@CopyNotNullTo(UserAccount.class)
public class UpdateUserRequest {

    @NotBlank
    @Size(max = 128)
    private String displayName;

    @NotNull
    private Boolean enabled;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String displayName, Boolean enabled) {
        this.displayName = displayName;
        this.enabled = enabled;
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

    public Boolean enabled() {
        return enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
