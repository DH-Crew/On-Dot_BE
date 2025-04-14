package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_MEMBER;

public class NotFoundMemberException extends NotFoundException {
  public NotFoundMemberException(Long memberId) {
    super(NOT_FOUND_MEMBER.getMessage().formatted(memberId));
  }

  @Override
  public String getErrorCode() {
    return NOT_FOUND_MEMBER.name();
  }
}
