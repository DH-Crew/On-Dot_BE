package com.dh.ondot.schedule.infra.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.ODSAY_TOO_CLOSE;

public class OdsayTooCloseException extends BadRequestException {
  public OdsayTooCloseException(String detail) {
    super(ODSAY_TOO_CLOSE.getMessage().formatted(detail));
  }
  @Override
  public String getErrorCode() {
    return ODSAY_TOO_CLOSE.name();
  }
}
