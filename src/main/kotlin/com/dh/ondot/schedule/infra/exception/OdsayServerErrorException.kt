package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_SERVER_ERROR

class OdsayServerErrorException(detail: String) :
    BadGatewayException(ODSAY_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = ODSAY_SERVER_ERROR.name
}
