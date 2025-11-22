package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_NO_STOP;

public class OdsayNoStopException extends BadRequestException {
    public OdsayNoStopException(String detail) {
        super(ODSAY_NO_STOP.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_NO_STOP.name();
    }
}
