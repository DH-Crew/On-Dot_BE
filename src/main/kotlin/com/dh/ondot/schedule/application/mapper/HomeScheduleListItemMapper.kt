package com.dh.ondot.schedule.application.mapper

import com.dh.ondot.schedule.application.dto.HomeScheduleListItem
import com.dh.ondot.schedule.domain.Schedule
import org.springframework.stereotype.Component

@Component
class HomeScheduleListItemMapper {

    fun toListOrderedByAlarmPriority(schedules: List<Schedule>): List<HomeScheduleListItem> {
        return schedules
            .map { HomeScheduleListItem.from(it) }
            .sortedWith(
                // 1순위: 활성화된 알람 우선 (true > false)
                compareByDescending<HomeScheduleListItem> { it.hasActiveAlarm }
                    // 2순위: 다음 알람 시간 오름차순 (null은 마지막)
                    .thenBy(nullsLast(), HomeScheduleListItem::nextAlarmAt)
            )
    }
}
