package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.InternalServerException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_UNHANDLED_ERROR;

public class OdsayUnhandledException extends InternalServerException {
    public OdsayUnhandledException(String detail) {
        super(ODSAY_UNHANDLED_ERROR.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_UNHANDLED_ERROR.name();
    }
}
