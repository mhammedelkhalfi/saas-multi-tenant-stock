package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class TenantProvisioningException extends BusinessException {

    public TenantProvisioningException(final String message) {
        super(ErrorCode.TENANT_PROVISIONING_ERROR, message);
    }
}
