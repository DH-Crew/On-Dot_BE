package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOCIAL_LOGIN;

public class InvalidOauthProviderException extends BadRequestException {
    public InvalidOauthProviderException(String type) {
        super(UNSUPPORTED_SOCIAL_LOGIN.getMessage().formatted(type));
    }

    @Override
    public String getErrorCode() {
        return UNSUPPORTED_SOCIAL_LOGIN.name();
    }
}
