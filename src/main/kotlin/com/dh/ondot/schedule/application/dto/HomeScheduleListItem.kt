package com.dh.ondot.schedule.application.dto

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.presentation.response.AlarmDto
import com.dh.ondot.schedule.domain.Schedule
import java.time.Instant
import java.time.LocalDateTime

data class HomeScheduleListItem(
    val scheduleId: Long?,
    val startLongitude: Double?,
    val startLatitude: Double?,
    val endLongitude: Double?,
    val endLatitude: Double?,
    val scheduleTitle: String?,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime?,
    val preparationAlarm: AlarmDto?,
    val departureAlarm: AlarmDto?,
    val hasActiveAlarm: Boolean,
    val preparationNote: String?,
    val nextAlarmAt: Instant?,
) {
    companion object {
        fun from(schedule: Schedule): HomeScheduleListItem {
            return HomeScheduleListItem(
                scheduleId = schedule.id,
                startLongitude = schedule.departurePlace!!.longitude,
                startLatitude = schedule.departurePlace!!.latitude,
                endLongitude = schedule.arrivalPlace!!.longitude,
                endLatitude = schedule.arrivalPlace!!.latitude,
                scheduleTitle = schedule.title,
                isRepeat = schedule.isRepeat,
                repeatDays = if (schedule.repeatDays == null) listOf() else schedule.repeatDays!!.toList(),
                appointmentAt = TimeUtils.toSeoulDateTime(schedule.appointmentAt),
                preparationAlarm = AlarmDto.of(schedule.preparationAlarm!!, schedule),
                departureAlarm = AlarmDto.of(schedule.departureAlarm!!, schedule),
                hasActiveAlarm = schedule.hasAnyActiveAlarm(),
                preparationNote = schedule.preparationNote,
                nextAlarmAt = schedule.calculateNextAlarmAt(),
            )
        }
    }
}
