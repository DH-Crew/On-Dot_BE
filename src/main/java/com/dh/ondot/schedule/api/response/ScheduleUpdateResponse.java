package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record ScheduleUpdateResponse(
        Long scheduleId,
        LocalDateTime updatedAt
) {
    public static ScheduleUpdateResponse of(Schedule schedule) {
        return new ScheduleUpdateResponse(schedule.getId(), schedule.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
    }
}
