package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadGatewayException;

import static com.dh.ondot.core.exception.ErrorCode.UNAVAILABLE_OPEN_AI_SERVER;

public class UnavailableOpenAiServerException extends BadGatewayException {
    public UnavailableOpenAiServerException() {
        super(UNAVAILABLE_OPEN_AI_SERVER.getMessage());
    }

    @Override
    public String getErrorCode() {
        return UNAVAILABLE_OPEN_AI_SERVER.name();
    }
}
