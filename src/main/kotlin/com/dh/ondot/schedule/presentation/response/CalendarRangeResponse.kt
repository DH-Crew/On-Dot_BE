package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.CalendarDayItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CalendarRangeResponse(
    val days: List<CalendarDayResponse>,
) {
    data class CalendarDayResponse(
        val date: LocalDate,
        val schedules: List<CalendarScheduleResponse>,
    )

    data class CalendarScheduleResponse(
        val scheduleId: Long,
        val title: String,
        val type: String,
        val isRepeat: Boolean,
        val appointmentAt: String,
    )

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        fun from(items: List<CalendarDayItem>): CalendarRangeResponse =
            CalendarRangeResponse(
                days = items.map { day ->
                    CalendarDayResponse(
                        date = day.date,
                        schedules = day.schedules.map { schedule ->
                            CalendarScheduleResponse(
                                scheduleId = schedule.scheduleId,
                                title = schedule.title,
                                type = schedule.type.name,
                                isRepeat = schedule.isRepeat,
                                appointmentAt = schedule.appointmentAt.format(DATE_TIME_FORMATTER),
                            )
                        },
                    )
                },
            )
    }
}
