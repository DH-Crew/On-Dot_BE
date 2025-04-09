package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_ADDRESS_TYPE;

public class UnsupportedAddressTypeException extends BadRequestException {
    public UnsupportedAddressTypeException(String addressType) {
        super(UNSUPPORTED_ADDRESS_TYPE.getMessage().formatted(addressType));
    }

    @Override
    public String getErrorCode() {
        return UNSUPPORTED_ADDRESS_TYPE.name();
    }
}
