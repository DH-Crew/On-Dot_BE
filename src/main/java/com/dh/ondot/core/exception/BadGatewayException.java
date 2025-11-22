package com.dh.ondot.core.exception;

public abstract class BadGatewayException extends RuntimeException {
    public BadGatewayException(String message) {
        super(message);
    }

    public abstract String getErrorCode();
}
