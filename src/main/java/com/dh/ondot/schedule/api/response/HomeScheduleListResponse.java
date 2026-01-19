package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record HomeScheduleListResponse(
        Long earliestAlarmId,
        LocalDateTime earliestAlarmAt,
        List<HomeScheduleListItem> scheduleList,
        boolean hasNext
) {
    public static HomeScheduleListResponse of(
            Long earliestAlarmId,
            Instant earliestAlarmAt,
            List<HomeScheduleListItem> list,
            boolean hasNext
    ) {
        return new HomeScheduleListResponse(
                earliestAlarmId,
                TimeUtils.toSeoulDateTime(earliestAlarmAt),
                list,
                hasNext
        );
    }
}
