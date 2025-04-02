package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.APPLE_SIGNATURE_INVALID;

public class AppleSignatureInvalidException extends UnauthorizedException {
    public AppleSignatureInvalidException() {
        super(APPLE_SIGNATURE_INVALID.getMessage());
    }

    @Override
    public String getErrorCode() {
        return APPLE_SIGNATURE_INVALID.name();
    }
}
