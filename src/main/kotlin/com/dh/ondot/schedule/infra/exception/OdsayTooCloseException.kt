package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_TOO_CLOSE

class OdsayTooCloseException(detail: String) :
    BadRequestException(ODSAY_TOO_CLOSE.message.format(detail)) {
    override val errorCode: String get() = ODSAY_TOO_CLOSE.name
}
