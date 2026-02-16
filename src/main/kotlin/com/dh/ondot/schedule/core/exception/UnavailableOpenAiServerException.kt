package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.UNAVAILABLE_OPEN_AI_SERVER

class UnavailableOpenAiServerException :
    BadGatewayException(UNAVAILABLE_OPEN_AI_SERVER.message) {
    override val errorCode: String get() = UNAVAILABLE_OPEN_AI_SERVER.name
}
