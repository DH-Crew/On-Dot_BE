package com.dh.ondot.core.exception;

public abstract class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public abstract String getErrorCode();
}
