package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_INVALID_URL

class EverytimeInvalidUrlException(detail: String) :
    BadRequestException(EVERYTIME_INVALID_URL.message.format(detail)) {
    override val errorCode: String get() = EVERYTIME_INVALID_URL.name
}
