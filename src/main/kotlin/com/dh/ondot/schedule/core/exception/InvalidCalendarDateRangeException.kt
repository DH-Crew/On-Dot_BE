package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode

class InvalidCalendarDateRangeException(startDate: String, endDate: String) :
    BadRequestException(ErrorCode.INVALID_CALENDAR_DATE_RANGE.message.format(startDate, endDate)) {
    override val errorCode: String get() = ErrorCode.INVALID_CALENDAR_DATE_RANGE.name
}

class CalendarDateRangeTooLargeException(days: Long) :
    BadRequestException(ErrorCode.CALENDAR_DATE_RANGE_TOO_LARGE.message.format(days)) {
    override val errorCode: String get() = ErrorCode.CALENDAR_DATE_RANGE_TOO_LARGE.name
}
