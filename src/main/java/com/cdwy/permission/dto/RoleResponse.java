package com.cdwy.permission.dto;

import cn.edu.xmu.clonefactory.CopyFrom;
import com.cdwy.permission.entity.Role;

@CopyFrom(Role.class)
public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean enabled;

    public RoleResponse() {
    }

    public RoleResponse(Long id, String code, String name, String description, boolean enabled) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public Long id() { return id; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String code() { return code; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String name() { return name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String description() { return description; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean enabled() { return enabled; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
