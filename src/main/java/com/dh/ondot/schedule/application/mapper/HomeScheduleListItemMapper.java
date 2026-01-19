package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class HomeScheduleListItemMapper {

    public List<HomeScheduleListItem> toListOrderedByAlarmPriority(List<Schedule> schedules) {
        return schedules.stream()
                .map(HomeScheduleListItem::from)
                .sorted(
                        Comparator
                                // 1순위: 활성화된 알람 우선 (true > false)
                                .comparing(HomeScheduleListItem::hasActiveAlarm, Comparator.reverseOrder())
                                // 2순위: 다음 알람 시간 오름차순 (null은 마지막)
                                .thenComparing(
                                        HomeScheduleListItem::nextAlarmAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                )
                .toList();
    }
}
