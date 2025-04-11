package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadRequestException;
import com.dh.ondot.core.exception.ErrorCode;

public class UnsupportedException extends BadRequestException {
    private final ErrorCode errorCode;

    public UnsupportedException(ErrorCode errorCode, String object) {
        super(errorCode.getMessage().formatted(object));
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return errorCode.name();
    }
}
