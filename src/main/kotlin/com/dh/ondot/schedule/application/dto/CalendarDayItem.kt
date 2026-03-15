package com.dh.ondot.schedule.application.dto

import java.time.LocalDate

data class CalendarDayItem(
    val date: LocalDate,
    val schedules: List<CalendarScheduleItem>,
)
