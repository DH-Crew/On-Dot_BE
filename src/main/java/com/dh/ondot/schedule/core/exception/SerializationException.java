package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.ErrorCode;
import com.dh.ondot.core.exception.InternalServerException;

public class SerializationException extends InternalServerException {
    private final ErrorCode errorCode;

    public SerializationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return errorCode.name();
    }
}
