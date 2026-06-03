package com.example.saas.config;

import com.example.saas.entities.Tenant;
import com.example.saas.entities.User;
import com.example.saas.enums.TenantStatus;
import com.example.saas.enums.UserRole;
import com.example.saas.properties.SystemAdminProperties;
import com.example.saas.respositories.TenantRepositorie;
import com.example.saas.respositories.UserRepositorie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Crée au démarrage un tenant système et un compte {@link UserRole#ROLE_PLATFORM_ADMIN}
 * si aucun utilisateur avec ce username n'existe encore.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class SystemAdminInitializer implements ApplicationRunner {

    private final SystemAdminProperties systemAdminProperties;
    private final TenantRepositorie tenantRepositorie;
    private final UserRepositorie userRepositorie;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        if (!systemAdminProperties.isEnabled()) {
            log.info("System admin auto-initialization is disabled");
            return;
        }

        if (!StringUtils.hasText(systemAdminProperties.getUsername())
                || !StringUtils.hasText(systemAdminProperties.getPassword())) {
            log.warn("System admin init skipped: username and password are required");
            return;
        }

        if (userRepositorie.existsByUsername(systemAdminProperties.getUsername())) {
            log.debug("System admin already exists (username: {})", systemAdminProperties.getUsername());
            return;
        }

        final Tenant systemTenant = tenantRepositorie
                .findByCompanyCode(systemAdminProperties.getTenantCompanyCode())
                .orElseGet(this::createSystemTenant);

        final String encodedPassword = passwordEncoder.encode(systemAdminProperties.getPassword());
        final User platformAdmin = User.builder()
                .username(systemAdminProperties.getUsername())
                .email(systemAdminProperties.getEmail())
                .password(encodedPassword)
                .firstName(systemAdminProperties.getFirstName())
                .lastName(systemAdminProperties.getLastName())
                .role(UserRole.ROLE_PLATFORM_ADMIN)
                .enabled(true)
                .deleted(false)
                .tenant(systemTenant)
                .build();

        userRepositorie.save(platformAdmin);

        log.warn(
                "Default platform admin created — username: '{}'. Change SYSTEM_ADMIN_PASSWORD in production!",
                systemAdminProperties.getUsername()
        );
    }

    private Tenant createSystemTenant() {
        final String encodedPassword = passwordEncoder.encode(systemAdminProperties.getPassword());
        final String adminFullName = systemAdminProperties.getFirstName() + " "
                + systemAdminProperties.getLastName();

        final Tenant tenant = Tenant.builder()
                .companyName(systemAdminProperties.getTenantCompanyName())
                .companyCode(systemAdminProperties.getTenantCompanyCode())
                .email(systemAdminProperties.getTenantEmail())
                .status(TenantStatus.ACTIVE)
                .adminFullName(adminFullName)
                .adminEmail(systemAdminProperties.getEmail())
                .adminUsername(systemAdminProperties.getUsername())
                .adminPassword(encodedPassword)
                .deleted(false)
                .build();

        final Tenant saved = tenantRepositorie.save(tenant);
        log.info("System tenant created (companyCode: {})", systemAdminProperties.getTenantCompanyCode());
        return saved;
    }
}
