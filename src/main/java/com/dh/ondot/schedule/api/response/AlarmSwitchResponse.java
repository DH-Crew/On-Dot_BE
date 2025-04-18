package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record AlarmSwitchResponse(
        Long scheduleId,
        Boolean isEnabled,
        LocalDateTime updatedAt
) {
    public static AlarmSwitchResponse from(Schedule schedule) {
        return new AlarmSwitchResponse(
                schedule.getId(),
                schedule.getDepartureAlarm().isEnabled(),
                schedule.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        );
    }
}
