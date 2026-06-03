package com.example.saas.respositories;

import com.example.saas.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepositorie extends JpaRepository<Tenant,String> {
    boolean existsByCompanyCode(String companyCode);
    boolean existsByEmail(String email);
    Optional<Tenant> findByCompanyCode(String companyCode);
}
