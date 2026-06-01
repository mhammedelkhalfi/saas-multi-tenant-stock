package com.example.saas.services;

import com.example.saas.common.PageResponse;
import com.example.saas.request.TenantRequest;
import com.example.saas.response.TenantResponse;

public interface TenanatService {

    void registerTenant(final TenantRequest request);
    void approuveTenant(final String tenantId);
    void activateTenant(final String tenantId);
    void desactivateTenant(final String tenantId);
    void suspendTenant(final String tenantId);
    PageResponse<TenantResponse> findAll(final int page, final int size);


}
