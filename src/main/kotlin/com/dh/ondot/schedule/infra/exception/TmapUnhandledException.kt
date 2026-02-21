package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.InternalServerException
import com.dh.ondot.core.exception.ErrorCode.TMAP_UNHANDLED_ERROR

class TmapUnhandledException(detail: String) :
    InternalServerException(TMAP_UNHANDLED_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_UNHANDLED_ERROR.name
}
