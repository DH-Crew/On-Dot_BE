package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record HomeScheduleListResponse(
        LocalDateTime earliestAlarmAt,
        List<HomeScheduleListItem> scheduleList,
        boolean hasNext
) {
    public static HomeScheduleListResponse of(Instant earliest, List<HomeScheduleListItem> list, boolean hasNext) {
        return new HomeScheduleListResponse(
                DateTimeUtils.toSeoulDateTime(earliest),
                list,
                hasNext
        );
    }
}
