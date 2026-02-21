package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_SERVER_ERROR

class EverytimeServerException(detail: String) :
    BadGatewayException(EVERYTIME_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = EVERYTIME_SERVER_ERROR.name
}
