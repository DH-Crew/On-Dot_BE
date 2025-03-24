package com.dh.ondot.core.exception;

public abstract class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public abstract String getErrorCode();
}