package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_BAD_INPUT

class OdsayBadInputException(detail: String) :
    BadRequestException(ODSAY_BAD_INPUT.message.format(detail)) {
    override val errorCode: String get() = ODSAY_BAD_INPUT.name
}
