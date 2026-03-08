package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_SERVICE_AREA

class TmapTransitServiceAreaException(detail: String) :
    BadRequestException(TMAP_TRANSIT_SERVICE_AREA.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_SERVICE_AREA.name
}
