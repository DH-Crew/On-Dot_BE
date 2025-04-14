package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;

public record ScheduleCreateResponse(
        Long scheduleId,
        LocalDateTime createdAt
) {
    public static ScheduleCreateResponse of(Schedule schedule) {
        return new ScheduleCreateResponse(schedule.getId(), schedule.getCreatedAt());
    }
}
