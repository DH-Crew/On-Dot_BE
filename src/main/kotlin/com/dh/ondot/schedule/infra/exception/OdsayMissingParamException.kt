package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.ODSAY_MISSING_PARAM

class OdsayMissingParamException(detail: String) :
    BadRequestException(ODSAY_MISSING_PARAM.message.format(detail)) {
    override val errorCode: String get() = ODSAY_MISSING_PARAM.name
}
