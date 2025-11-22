package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;

public record AlarmSwitchResponse(
        Long scheduleId,
        Boolean isEnabled,
        LocalDateTime updatedAt
) {
    public static AlarmSwitchResponse from(Schedule schedule) {
        return new AlarmSwitchResponse(
                schedule.getId(),
                schedule.getDepartureAlarm().isEnabled(),
                TimeUtils.toSeoulDateTime(schedule.getUpdatedAt())
        );
    }
}
