package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_ADDRESS;

public class NotFoundAddressException extends NotFoundException {
    public NotFoundAddressException(Long addressId) {
        super(NOT_FOUND_ADDRESS.getMessage().formatted(addressId));
    }

    @Override
    public String getErrorCode() {
        return NOT_FOUND_ADDRESS.name();
    }
}
