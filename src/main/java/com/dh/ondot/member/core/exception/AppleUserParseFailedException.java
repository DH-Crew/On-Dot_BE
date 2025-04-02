package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.APPLE_USER_PARSE_FAILED;

public class AppleUserParseFailedException extends NotFoundException {
    public AppleUserParseFailedException() {
        super(APPLE_USER_PARSE_FAILED.getMessage());
    }

    @Override
    public String getErrorCode() {
        return APPLE_USER_PARSE_FAILED.name();
    }
}
