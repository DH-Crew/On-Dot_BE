package com.dh.ondot.schedule.application.dto

import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import java.time.LocalDateTime

data class CalendarScheduleItem(
    val scheduleId: Long,
    val title: String,
    val type: CalendarScheduleType,
    val isRepeat: Boolean,
    val appointmentAt: LocalDateTime,
)
