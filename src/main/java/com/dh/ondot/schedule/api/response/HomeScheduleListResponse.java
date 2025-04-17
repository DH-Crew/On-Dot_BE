package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.app.dto.HomeScheduleListItem;

import java.time.LocalDateTime;
import java.util.List;

public record HomeScheduleListResponse(
        LocalDateTime earliestAlarmTime,
        List<HomeScheduleListItem> scheduleList,
        boolean hasNext
) {
    public static HomeScheduleListResponse of(LocalDateTime earliest, List<HomeScheduleListItem> list, boolean hasNext) {
        return new HomeScheduleListResponse(
                earliest,
                list,
                hasNext
        );
    }
}
