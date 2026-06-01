package com.example.saas.controllers;

import com.example.saas.common.PageResponse;
import com.example.saas.response.TenantResponse;
import com.example.saas.services.TenanatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "Tenant API")
public class TenantController {

    private final TenanatService tenanatService;

    @PostMapping("/approve/{tenant-id}")
    public ResponseEntity<Void> approveTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.tenanatService.approuveTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/activate/{tenant-id}")
    public ResponseEntity<Void> activateTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.tenanatService.activateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/deactivate/{tenant-id}")
    public ResponseEntity<Void> deactivateTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.tenanatService.desactivateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/suspend/{tenant-id}")
    public ResponseEntity<Void> suspendTenant(
            @PathVariable("tenant-id")
            final String  tenantId
    ) {
        this.tenanatService.suspendTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<TenantResponse>> findAllTenants(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(this.tenanatService.findAll(page, size));
    }
    
}
