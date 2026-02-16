package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.TOKEN_MISSING
import com.dh.ondot.core.exception.UnauthorizedException

class TokenMissingException :
    UnauthorizedException(TOKEN_MISSING.message) {
    override val errorCode: String get() = TOKEN_MISSING.name
}
