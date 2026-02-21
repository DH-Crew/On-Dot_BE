package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_EMPTY_TIMETABLE

class EverytimeEmptyTimetableException :
    NotFoundException(EVERYTIME_EMPTY_TIMETABLE.message) {
    override val errorCode: String get() = EVERYTIME_EMPTY_TIMETABLE.name
}
