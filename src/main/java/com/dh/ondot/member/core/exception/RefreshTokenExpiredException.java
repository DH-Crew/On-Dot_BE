package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.REFRESH_TOKEN_EXPIRED;

public class RefreshTokenExpiredException extends UnauthorizedException {
    public RefreshTokenExpiredException() {
        super(REFRESH_TOKEN_EXPIRED.getMessage());
    }

    @Override
    public String getErrorCode() {
        return REFRESH_TOKEN_EXPIRED.name();
    }
}