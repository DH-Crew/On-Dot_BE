package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.INVALID_ALARM_TRIGGER_ACTION

class InvalidAlarmTriggerActionException(value: String) :
    BadRequestException(INVALID_ALARM_TRIGGER_ACTION.message.format(value)) {
    override val errorCode: String get() = INVALID_ALARM_TRIGGER_ACTION.name
}
