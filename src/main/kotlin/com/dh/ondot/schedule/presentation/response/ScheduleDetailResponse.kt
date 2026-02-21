package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.presentation.request.PlaceDto
import com.dh.ondot.schedule.domain.Schedule
import java.time.LocalDateTime

data class ScheduleDetailResponse(
    val scheduleId: Long?,
    val title: String?,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime?,
    val preparationAlarm: AlarmDto,
    val departureAlarm: AlarmDto,
    val departurePlace: PlaceDto,
    val arrivalPlace: PlaceDto,
    val transportType: TransportType,
) {
    companion object {
        @JvmStatic
        fun from(s: Schedule): ScheduleDetailResponse {
            return ScheduleDetailResponse(
                scheduleId = s.id,
                title = s.title,
                isRepeat = s.isRepeat,
                repeatDays = s.repeatDays?.toList() ?: emptyList(),
                appointmentAt = TimeUtils.toSeoulDateTime(s.appointmentAt),
                preparationAlarm = AlarmDto.of(s.preparationAlarm!!),
                departureAlarm = AlarmDto.of(s.departureAlarm!!),
                departurePlace = PlaceDto.from(s.departurePlace!!),
                arrivalPlace = PlaceDto.from(s.arrivalPlace!!),
                transportType = s.transportType,
            )
        }
    }
}
