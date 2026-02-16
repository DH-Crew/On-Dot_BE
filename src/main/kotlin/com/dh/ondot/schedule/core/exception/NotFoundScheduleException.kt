package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_SCHEDULE
import com.dh.ondot.core.exception.NotFoundException

class NotFoundScheduleException(scheduleId: Long) :
    NotFoundException(NOT_FOUND_SCHEDULE.message.format(scheduleId)) {
    override val errorCode: String get() = NOT_FOUND_SCHEDULE.name
}
