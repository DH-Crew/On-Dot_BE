package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_HOME_ADDRESS;

public class NotFoundHomeAddressException extends NotFoundException {
    public NotFoundHomeAddressException(Long memberId) {
        super(NOT_FOUND_HOME_ADDRESS.getMessage().formatted(memberId));
    }

    @Override
    public String getErrorCode() {
        return NOT_FOUND_HOME_ADDRESS.name();
    }
}
