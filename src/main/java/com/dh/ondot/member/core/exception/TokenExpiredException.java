package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.TOKEN_EXPIRED;

public class TokenExpiredException extends UnauthorizedException {

    public TokenExpiredException() {
        super(TOKEN_EXPIRED.getMessage());
    }

    @Override
    public String getErrorCode() {
        return TOKEN_EXPIRED.name();
    }
}