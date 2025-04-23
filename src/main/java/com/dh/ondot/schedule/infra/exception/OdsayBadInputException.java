package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_BAD_INPUT;

public class OdsayBadInputException extends BadRequestException {
    public OdsayBadInputException(String detail) {
        super(ODSAY_BAD_INPUT.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_BAD_INPUT.name();
    }
}
