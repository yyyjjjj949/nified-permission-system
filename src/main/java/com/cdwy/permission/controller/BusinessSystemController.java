package com.cdwy.permission.controller;

import com.cdwy.permission.dto.CreateSystemRequest;
import com.cdwy.permission.dto.SystemResponse;
import com.cdwy.permission.dto.UpdateSystemRequest;
import com.cdwy.permission.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/systems")
public class BusinessSystemController {

    private final RbacService rbacService;

    public BusinessSystemController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping
    public ResponseEntity<SystemResponse> create(@Valid @RequestBody CreateSystemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createSystem(request));
    }

    @GetMapping
    public List<SystemResponse> list() {
        return rbacService.listSystems();
    }

    @PutMapping("/{systemId}")
    public SystemResponse update(
            @PathVariable Long systemId,
            @Valid @RequestBody UpdateSystemRequest request) {
        return rbacService.updateSystem(systemId, request);
    }

    @DeleteMapping("/{systemId}")
    public ResponseEntity<Void> delete(@PathVariable Long systemId) {
        rbacService.deleteSystem(systemId);
        return ResponseEntity.noContent().build();
    }
}
