package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class DuplicateResouceException extends BusinessException {

    public DuplicateResouceException(final String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}
