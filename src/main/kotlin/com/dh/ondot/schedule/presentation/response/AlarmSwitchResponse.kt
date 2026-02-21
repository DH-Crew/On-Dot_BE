package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.Schedule
import java.time.LocalDateTime

data class AlarmSwitchResponse(
    val scheduleId: Long?,
    val isEnabled: Boolean,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(schedule: Schedule): AlarmSwitchResponse {
            return AlarmSwitchResponse(
                scheduleId = schedule.id,
                isEnabled = schedule.departureAlarm!!.isEnabled,
                updatedAt = TimeUtils.toSeoulDateTime(schedule.updatedAt),
            )
        }
    }
}
