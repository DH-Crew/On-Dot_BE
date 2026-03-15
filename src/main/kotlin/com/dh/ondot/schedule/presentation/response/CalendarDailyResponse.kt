package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.CalendarDailyItem
import java.time.format.DateTimeFormatter

data class CalendarDailyResponse(
    val schedules: List<CalendarDailyScheduleResponse>,
) {
    data class CalendarDailyScheduleResponse(
        val scheduleId: Long,
        val type: String,
        val title: String,
        val isRepeat: Boolean,
        val repeatDays: List<Int>,
        val appointmentAt: String,
        val preparationAlarm: AlarmDto?,
        val departureAlarm: AlarmDto?,
        val hasActiveAlarm: Boolean,
        val startLongitude: Double?,
        val startLatitude: Double?,
        val endLongitude: Double?,
        val endLatitude: Double?,
        val preparationNote: String?,
    )

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        fun from(items: List<CalendarDailyItem>): CalendarDailyResponse =
            CalendarDailyResponse(
                schedules = items.map { item ->
                    CalendarDailyScheduleResponse(
                        scheduleId = item.scheduleId,
                        type = item.type.name,
                        title = item.title,
                        isRepeat = item.isRepeat,
                        repeatDays = item.repeatDays,
                        appointmentAt = item.appointmentAt.format(DATE_TIME_FORMATTER),
                        preparationAlarm = item.preparationAlarm,
                        departureAlarm = item.departureAlarm,
                        hasActiveAlarm = item.hasActiveAlarm,
                        startLongitude = item.startLongitude,
                        startLatitude = item.startLatitude,
                        endLongitude = item.endLongitude,
                        endLatitude = item.endLatitude,
                        preparationNote = item.preparationNote,
                    )
                },
            )
    }
}
