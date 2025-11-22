package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_NO_RESULT;

public class OdsayNoResultException extends BadRequestException {
    public OdsayNoResultException(String detail) {
        super(ODSAY_NO_RESULT.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_NO_RESULT.name();
    }
}
