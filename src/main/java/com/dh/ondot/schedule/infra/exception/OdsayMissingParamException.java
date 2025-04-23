package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_MISSING_PARAM;

public class OdsayMissingParamException extends BadRequestException {
    public OdsayMissingParamException(String detail) {
        super(ODSAY_MISSING_PARAM.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_MISSING_PARAM.name();
    }
}
