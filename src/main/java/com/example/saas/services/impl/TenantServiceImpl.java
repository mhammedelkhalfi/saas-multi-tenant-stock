package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.entities.Tenant;
import com.example.saas.entities.User;
import com.example.saas.enums.TenantStatus;
import com.example.saas.enums.UserRole;
import com.example.saas.exceptions.DuplicateResouceException;
import com.example.saas.exceptions.InvalidRequestException;
import com.example.saas.mappers.TenanatMapper;
import com.example.saas.request.TenantRequest;
import com.example.saas.response.TenantResponse;
import com.example.saas.respositories.TenantRepositorie;
import com.example.saas.respositories.UserRepositorie;
import com.example.saas.services.ProvisioningService;
import com.example.saas.services.TenanatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenanatService {

    private final TenantRepositorie tenantRepositorie;
    private final TenanatMapper tenanatMapper;
    private  final PasswordEncoder passwordEncoder;
    private  final UserRepositorie userRepositorie;
    private final ProvisioningService provisioningService;




    @Override
    @Transactional
    public void registerTenant(TenantRequest request) {
        if(this.tenantRepositorie.existsByCompanyCode(request.getCompanyCode())){
            throw new DuplicateResouceException("Company code already exists");
        }
        if(this.tenantRepositorie.existsByEmail(request.getEmail())){
            throw new DuplicateResouceException("Email already exists");
        }
        final Tenant tenant = this.tenanatMapper.toEntity(request);
        tenant.setAdminPassword(this.passwordEncoder.encode(request.getAdminPassword()));
        tenant.setStatus(TenantStatus.PENDING);
        this.tenantRepositorie.save(tenant);
    }

    @Override
    public void approuveTenant(String tenantId) {

        // check if tenant exists
        final Tenant tenant = this.tenantRepositorie.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        // activate tenant
        tenant.setStatus(TenantStatus.ACTIVE);
        this.tenantRepositorie.save(tenant);

        try {
            // provision the schema for the tenant
            this.provisioningService.provisionTenant(tenant);
            // create initial admin user
            createInitiaAdminUser(tenant);
        } catch (final Exception e) {
            rollbackTenantStatus(tenant);
        }
    }

    @Override
    public void activateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepositorie.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.PENDING) {
            throw new InvalidRequestException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        this.tenantRepositorie.save(tenant);
    }

    @Override
    public void desactivateTenant(String tenantId) {
        final Tenant tenant = this.tenantRepositorie.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidRequestException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.INACTIVE);
        this.tenantRepositorie.save(tenant);
    }

    @Override
    public void suspendTenant(String tenantId) {
        final Tenant tenant = this.tenantRepositorie.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant does not exist"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidRequestException("Tenant is not pending");
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        this.tenantRepositorie.save(tenant);
    }

    @Override
    public PageResponse<TenantResponse> findAll(int page, int size) {
        return null;
    }
    private void rollbackTenantStatus(final Tenant tenant) {
        tenant.setStatus(TenantStatus.PENDING);
        this.tenantRepositorie.save(tenant);
    }

    private void createInitiaAdminUser(final Tenant tenant) {
        // check if the user already exists
        if (this.userRepositorie.existsByUsername(tenant.getAdminUsername())) {
            throw new DuplicateResouceException("User already exists");
        }

        final User adminUser = User.builder()
                .username(tenant.getAdminUsername())
                .email(tenant.getAdminEmail())
                .firstName(extractFirstName(tenant.getAdminFullName()))
                .lastName(extractLastName(tenant.getAdminFullName()))
                .password(this.passwordEncoder.encode(tenant.getAdminPassword()))
                .role(UserRole.ROLE_COMPANY_ADMIN)
                .tenant(tenant)
                .enabled(true)
                .build();
        this.userRepositorie.save(adminUser);
        log.info("Created initial admin user for tenant {}", tenant.getId());
    }

    private String extractFirstName(final String fullName) {
        return fullName.split(" ")[0];
    }

    private String extractLastName(final String fullName) {
        return fullName.split(" ").length > 1 ? fullName.split(" ")[1] : fullName;
    }
}
