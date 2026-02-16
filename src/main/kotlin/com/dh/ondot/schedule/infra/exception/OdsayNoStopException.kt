package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_NO_STOP

class OdsayNoStopException(detail: String) :
    BadRequestException(ODSAY_NO_STOP.message.format(detail)) {
    override val errorCode: String get() = ODSAY_NO_STOP.name
}
