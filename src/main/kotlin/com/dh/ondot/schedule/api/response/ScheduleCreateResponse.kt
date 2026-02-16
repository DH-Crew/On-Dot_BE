package com.dh.ondot.schedule.api.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.Schedule
import java.time.Duration
import java.time.LocalDateTime

data class ScheduleCreateResponse(
    val scheduleId: Long?,
    val estimateTime: Int,
    val preparationTriggeredAt: LocalDateTime?,
    val departureTriggeredAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        @JvmStatic
        fun of(schedule: Schedule): ScheduleCreateResponse {
            val prepAt = TimeUtils.toSeoulDateTime(
                schedule.preparationAlarm!!.triggeredAt
            )
            val deptAt = TimeUtils.toSeoulDateTime(
                schedule.departureAlarm!!.triggeredAt
            )
            val created = TimeUtils.toSeoulDateTime(schedule.createdAt)
            val estimateTime = Math.toIntExact(
                Duration.between(deptAt, TimeUtils.toSeoulDateTime(schedule.appointmentAt))
                    .toMinutes()
            )

            return ScheduleCreateResponse(
                scheduleId = schedule.id,
                estimateTime = estimateTime,
                preparationTriggeredAt = prepAt,
                departureTriggeredAt = deptAt,
                createdAt = created,
            )
        }
    }
}
