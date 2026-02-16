package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.TOKEN_INVALID
import com.dh.ondot.core.exception.UnauthorizedException

class TokenInvalidException :
    UnauthorizedException(TOKEN_INVALID.message) {
    override val errorCode: String get() = TOKEN_INVALID.name
}
