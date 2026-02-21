package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.TMAP_SERVER_ERROR

class TmapServerErrorException(detail: String) :
    BadGatewayException(TMAP_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_SERVER_ERROR.name
}
