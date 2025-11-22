package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.TOKEN_MISSING;

public class TokenMissingException extends UnauthorizedException {

    public TokenMissingException() {
        super(TOKEN_MISSING.getMessage());
    }

    @Override
    public String getErrorCode() {
        return TOKEN_MISSING.name();
    }
}
