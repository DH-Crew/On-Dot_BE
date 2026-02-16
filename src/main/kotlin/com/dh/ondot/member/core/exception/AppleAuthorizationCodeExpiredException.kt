package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.APPLE_AUTHORIZATION_CODE_EXPIRED
import com.dh.ondot.core.exception.UnauthorizedException

class AppleAuthorizationCodeExpiredException :
    UnauthorizedException(APPLE_AUTHORIZATION_CODE_EXPIRED.message) {
    override val errorCode: String get() = APPLE_AUTHORIZATION_CODE_EXPIRED.name
}
