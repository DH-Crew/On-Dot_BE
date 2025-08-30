package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.ConflictException;

import static com.dh.ondot.core.exception.ErrorCode.ALREADY_ONBOARDED_MEMBER;

public class AlreadyOnboardedException extends ConflictException {
    public AlreadyOnboardedException(Long memberId) {
        super(ALREADY_ONBOARDED_MEMBER.getMessage().formatted(memberId));
    }

    @Override
    public String getErrorCode() {
        return ALREADY_ONBOARDED_MEMBER.name();
    }
}
