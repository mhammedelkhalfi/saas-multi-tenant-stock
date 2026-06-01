package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(final String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
