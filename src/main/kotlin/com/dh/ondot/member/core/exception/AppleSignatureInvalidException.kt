package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.APPLE_SIGNATURE_INVALID
import com.dh.ondot.core.exception.UnauthorizedException

class AppleSignatureInvalidException :
    UnauthorizedException(APPLE_SIGNATURE_INVALID.message) {
    override val errorCode: String get() = APPLE_SIGNATURE_INVALID.name
}
