package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_SERVER_ERROR

class TmapTransitServerErrorException(detail: String) :
    BadGatewayException(TMAP_TRANSIT_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_SERVER_ERROR.name
}
