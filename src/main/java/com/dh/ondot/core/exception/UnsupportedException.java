package com.dh.ondot.core.exception;

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
