package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_ALARM
import com.dh.ondot.core.exception.NotFoundException

class NotFoundAlarmException(alarmId: Long) :
    NotFoundException(NOT_FOUND_ALARM.message.format(alarmId)) {
    override val errorCode: String get() = NOT_FOUND_ALARM.name
}
