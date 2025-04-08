package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.INVALID_TOKEN_HEADER;

public class InvalidTokenHeaderException extends UnauthorizedException {
    public InvalidTokenHeaderException() {
        super(INVALID_TOKEN_HEADER.getMessage());
    }

    @Override
    public String getErrorCode() {
        return INVALID_TOKEN_HEADER.name();
    }
}
