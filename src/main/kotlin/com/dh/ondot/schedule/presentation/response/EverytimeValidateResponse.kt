package com.dh.ondot.schedule.presentation.response

import java.time.DayOfWeek
import java.time.LocalTime

data class EverytimeValidateResponse(
    val timetable: Map<DayOfWeek, List<TimetableEntry>>,
) {
    data class TimetableEntry(
        val courseName: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
    )
}
