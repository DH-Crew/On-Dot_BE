package com.dh.ondot.schedule.application.dto

import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.presentation.response.AlarmDto
import java.time.LocalDateTime

data class CalendarDailyItem(
    val scheduleId: Long,
    val type: CalendarScheduleType,
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val preparationAlarm: AlarmDto?,
    val departureAlarm: AlarmDto?,
    val hasActiveAlarm: Boolean,
    val startLongitude: Double?,
    val startLatitude: Double?,
    val endLongitude: Double?,
    val endLatitude: Double?,
    val preparationNote: String?,
)
