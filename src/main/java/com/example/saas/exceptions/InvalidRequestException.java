package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;

public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(final String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}
