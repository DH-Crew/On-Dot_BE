package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadGatewayException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_SERVER_ERROR;

public class OdsayServerErrorException extends BadGatewayException {
    public OdsayServerErrorException(String detail) {
        super(ODSAY_SERVER_ERROR.getMessage().formatted(detail));
    }
    @Override
    public String getErrorCode() {
        return ODSAY_SERVER_ERROR.name();
    }
}
