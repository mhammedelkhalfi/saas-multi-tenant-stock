package com.example.saas.entities;

import com.example.saas.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant extends AbstractEntity {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_code", nullable = false, unique = true)
    private String companyCode;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status = TenantStatus.PENDING;

    @Column(name = "admin_full_name", nullable = false)
    private String adminFullName;

    @Column(name = "admin_email", nullable = false, unique = true)
    private String adminEmail;

    @Column(name = "admin_username", nullable = false, unique = true)
    private String adminUsername;

    @Column(name = "admin_password", nullable = false)
    private String adminPassword;
}
