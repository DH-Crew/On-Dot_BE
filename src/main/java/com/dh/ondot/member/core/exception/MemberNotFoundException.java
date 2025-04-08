package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.MEMBER_NOT_FOUND;

public class MemberNotFoundException extends NotFoundException {
  public MemberNotFoundException(Long memberId) {
    super(MEMBER_NOT_FOUND.getMessage().formatted(memberId));
  }

  @Override
  public String getErrorCode() {
    return MEMBER_NOT_FOUND.name();
  }
}
