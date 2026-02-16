package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_SERVICE_AREA

class OdsayServiceAreaException(detail: String) :
    BadRequestException(ODSAY_SERVICE_AREA.message.format(detail)) {
    override val errorCode: String get() = ODSAY_SERVICE_AREA.name
}
