package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.Schedule;

import java.time.Duration;
import java.time.LocalDateTime;

public record ScheduleCreateResponse(
        Long scheduleId,
        int estimateTime,
        LocalDateTime preparationTriggeredAt,
        LocalDateTime departureTriggeredAt,
        LocalDateTime createdAt
) {
    public static ScheduleCreateResponse of(Schedule schedule) {
        LocalDateTime prepAt = TimeUtils.toSeoulDateTime(
                schedule.getPreparationAlarm().getTriggeredAt()
        );
        LocalDateTime deptAt = TimeUtils.toSeoulDateTime(
                schedule.getDepartureAlarm().getTriggeredAt()
        );
        LocalDateTime created = TimeUtils.toSeoulDateTime(schedule.getCreatedAt());
        int estimateTime = Math.toIntExact(
                Duration.between(deptAt, TimeUtils.toSeoulDateTime(schedule.getAppointmentAt()))
                        .toMinutes()
        );

        return new ScheduleCreateResponse(
                schedule.getId(),
                estimateTime,
                prepAt,
                deptAt,
                created
        );
    }
}
