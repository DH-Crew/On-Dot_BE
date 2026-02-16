package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.INVALID_TOKEN_HEADER
import com.dh.ondot.core.exception.UnauthorizedException

class InvalidTokenHeaderException :
    UnauthorizedException(INVALID_TOKEN_HEADER.message) {
    override val errorCode: String get() = INVALID_TOKEN_HEADER.name
}
