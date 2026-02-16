package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.ErrorCode.ODSAY_UNHANDLED_ERROR
import com.dh.ondot.core.exception.InternalServerException

class OdsayUnhandledException(detail: String) :
    InternalServerException(ODSAY_UNHANDLED_ERROR.message.format(detail)) {
    override val errorCode: String get() = ODSAY_UNHANDLED_ERROR.name
}
