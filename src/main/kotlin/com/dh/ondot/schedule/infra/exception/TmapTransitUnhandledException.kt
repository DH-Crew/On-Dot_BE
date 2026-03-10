package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.InternalServerException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_UNHANDLED_ERROR

class TmapTransitUnhandledException(detail: String) :
    InternalServerException(TMAP_TRANSIT_UNHANDLED_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_UNHANDLED_ERROR.name
}
