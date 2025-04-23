package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_SERVICE_AREA;

public class OdsayServiceAreaException extends BadRequestException {
    public OdsayServiceAreaException(String detail) {
        super(ODSAY_SERVICE_AREA.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_SERVICE_AREA.name();
    }
}
