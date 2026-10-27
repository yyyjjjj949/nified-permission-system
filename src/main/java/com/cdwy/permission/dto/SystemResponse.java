package com.cdwy.permission.dto;

import cn.edu.xmu.clonefactory.CopyFrom;
import com.cdwy.permission.entity.BusinessSystem;

@CopyFrom(BusinessSystem.class)
public class SystemResponse {

    private Long id;
    private String code;
    private String name;
    private String baseUrl;
    private boolean enabled;

    public SystemResponse() {
    }

    public SystemResponse(Long id, String code, String name, String baseUrl, boolean enabled) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.baseUrl = baseUrl;
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
    public String baseUrl() { return baseUrl; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public boolean enabled() { return enabled; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
