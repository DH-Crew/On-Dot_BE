package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.TMAP_NO_RESULT

class TmapNoResultException(detail: String) :
    NotFoundException(TMAP_NO_RESULT.message.format(detail)) {
    override val errorCode: String get() = TMAP_NO_RESULT.name
}
