package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.TOKEN_INVALID;

public class TokenInvalidException extends UnauthorizedException {

    public TokenInvalidException() {
        super(TOKEN_INVALID.getMessage());
    }

    @Override
    public String getErrorCode() {
        return TOKEN_INVALID.name();
    }
}