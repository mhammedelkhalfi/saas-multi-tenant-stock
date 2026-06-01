package com.example.saas.services;

import com.example.saas.entities.Tenant;

public interface ProvisioningService {
    void provisionTenant(final Tenant tenant);

}
