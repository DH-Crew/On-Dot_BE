package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.TOKEN_BLACKLISTED
import com.dh.ondot.core.exception.UnauthorizedException

class TokenBlacklistedException :
    UnauthorizedException(TOKEN_BLACKLISTED.message) {
    override val errorCode: String get() = TOKEN_BLACKLISTED.name
}
