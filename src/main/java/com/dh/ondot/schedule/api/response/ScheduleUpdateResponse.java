package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;

public record ScheduleUpdateResponse(
        Long scheduleId,
        LocalDateTime updatedAt
) {
    public static ScheduleUpdateResponse of(Schedule schedule) {
        return new ScheduleUpdateResponse(schedule.getId(), schedule.getUpdatedAt());
    }
}
