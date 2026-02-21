package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_NOT_FOUND

class EverytimeNotFoundException :
    NotFoundException(EVERYTIME_NOT_FOUND.message) {
    override val errorCode: String get() = EVERYTIME_NOT_FOUND.name
}
