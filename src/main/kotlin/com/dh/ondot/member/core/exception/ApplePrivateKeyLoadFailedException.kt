package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.APPLE_PRIVATE_KEY_LOAD_FAILED
import com.dh.ondot.core.exception.InternalServerException

class ApplePrivateKeyLoadFailedException :
    InternalServerException(APPLE_PRIVATE_KEY_LOAD_FAILED.message) {
    override val errorCode: String get() = APPLE_PRIVATE_KEY_LOAD_FAILED.name
}
