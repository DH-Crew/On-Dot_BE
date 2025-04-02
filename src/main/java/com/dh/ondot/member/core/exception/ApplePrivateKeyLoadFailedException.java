package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.InternalServerException;

import static com.dh.ondot.core.exception.ErrorCode.APPLE_PRIVATE_KEY_LOAD_FAILED;

public class ApplePrivateKeyLoadFailedException extends InternalServerException {
    public ApplePrivateKeyLoadFailedException() {
        super(APPLE_PRIVATE_KEY_LOAD_FAILED.getMessage());
    }

    @Override
    public String getErrorCode() {
        return APPLE_PRIVATE_KEY_LOAD_FAILED.name();
    }
}
