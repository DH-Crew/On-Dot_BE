package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_MAP_PROVIDER;

public class UnsupportedMapProviderException extends BadRequestException {
    public UnsupportedMapProviderException(String mapProvider) {
        super(UNSUPPORTED_MAP_PROVIDER.getMessage().formatted(mapProvider));
    }

    @Override
    public String getErrorCode() {
        return UNSUPPORTED_MAP_PROVIDER.name();
    }
}
