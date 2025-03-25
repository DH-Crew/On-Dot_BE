package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.UnauthorizedException;

import static com.dh.ondot.core.exception.ErrorCode.TOKEN_BLACKLISTED;

public class TokenBlacklistedException extends UnauthorizedException {

  public TokenBlacklistedException() {
    super(TOKEN_BLACKLISTED.getMessage());
  }

  @Override
  public String getErrorCode() {
    return TOKEN_BLACKLISTED.name();
  }
}