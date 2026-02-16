package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.APPLE_USER_PARSE_FAILED
import com.dh.ondot.core.exception.NotFoundException

class AppleUserParseFailedException :
    NotFoundException(APPLE_USER_PARSE_FAILED.message) {
    override val errorCode: String get() = APPLE_USER_PARSE_FAILED.name
}
