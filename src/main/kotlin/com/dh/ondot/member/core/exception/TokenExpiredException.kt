package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.TOKEN_EXPIRED
import com.dh.ondot.core.exception.UnauthorizedException

class TokenExpiredException :
    UnauthorizedException(TOKEN_EXPIRED.message) {
    override val errorCode: String get() = TOKEN_EXPIRED.name
}
