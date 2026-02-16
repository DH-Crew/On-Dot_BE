package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.REFRESH_TOKEN_EXPIRED
import com.dh.ondot.core.exception.UnauthorizedException

class RefreshTokenExpiredException :
    UnauthorizedException(REFRESH_TOKEN_EXPIRED.message) {
    override val errorCode: String get() = REFRESH_TOKEN_EXPIRED.name
}
