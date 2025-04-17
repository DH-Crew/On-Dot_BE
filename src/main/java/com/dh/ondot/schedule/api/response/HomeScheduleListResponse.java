package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.app.dto.HomeScheduleListItem;

import java.time.LocalDateTime;
import java.util.List;

public record HomeScheduleListResponse(
        boolean isOnboardingCompleted,
        LocalDateTime earliestAlarmTime,
        List<HomeScheduleListItem> scheduleList,
        boolean hasNext
) {
    public static HomeScheduleListResponse of(boolean isOnboardingCompleted, LocalDateTime earliest, List<HomeScheduleListItem> list, boolean hasNext) {
        return new HomeScheduleListResponse(
                isOnboardingCompleted,
                earliest,
                list,
                hasNext
        );
    }
}
