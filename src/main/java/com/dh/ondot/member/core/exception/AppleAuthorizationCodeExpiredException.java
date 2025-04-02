package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.APPLE_AUTHORIZATION_CODE_EXPIRED;

public class AppleAuthorizationCodeExpiredException extends UnauthorizedException {
    public AppleAuthorizationCodeExpiredException() {
        super(APPLE_AUTHORIZATION_CODE_EXPIRED.getMessage());
    }

    @Override
    public String getErrorCode() {
        return APPLE_AUTHORIZATION_CODE_EXPIRED.name();
    }
}
