package com.dh.ondot.schedule.api.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.Schedule
import java.time.LocalDateTime

data class ScheduleUpdateResponse(
    val scheduleId: Long?,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        @JvmStatic
        fun of(schedule: Schedule): ScheduleUpdateResponse {
            return ScheduleUpdateResponse(
                scheduleId = schedule.id,
                updatedAt = TimeUtils.toSeoulDateTime(schedule.updatedAt),
            )
        }
    }
}
