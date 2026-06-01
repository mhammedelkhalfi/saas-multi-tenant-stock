package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(final String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
