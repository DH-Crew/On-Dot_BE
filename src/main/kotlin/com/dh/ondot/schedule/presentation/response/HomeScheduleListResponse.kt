package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem
import java.time.Instant
import java.time.LocalDateTime

data class HomeScheduleListResponse(
    val earliestAlarmId: Long?,
    val earliestAlarmAt: LocalDateTime?,
    val scheduleList: List<HomeScheduleListItem>,
    val hasNext: Boolean,
) {
    companion object {
        fun of(
            earliestAlarmId: Long?,
            earliestAlarmAt: Instant?,
            list: List<HomeScheduleListItem>,
            hasNext: Boolean,
        ): HomeScheduleListResponse {
            return HomeScheduleListResponse(
                earliestAlarmId = earliestAlarmId,
                earliestAlarmAt = TimeUtils.toSeoulDateTime(earliestAlarmAt),
                scheduleList = list,
                hasNext = hasNext,
            )
        }
    }
}
