package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(final String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
