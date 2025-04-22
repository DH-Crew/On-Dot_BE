package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.InternalServerException;

import static com.dh.ondot.core.exception.ErrorCode.UNHANDLED_OPEN_AI;

public class UnhandledOpenAiException extends InternalServerException {
    public UnhandledOpenAiException() {
        super(UNHANDLED_OPEN_AI.getMessage());
    }

    @Override
    public String getErrorCode() {
        return UNHANDLED_OPEN_AI.name();
    }
}
