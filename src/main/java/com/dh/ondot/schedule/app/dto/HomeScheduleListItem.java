package com.dh.ondot.schedule.app.dto;

import com.dh.ondot.schedule.domain.Schedule;

import java.time.*;
import java.util.List;

public record HomeScheduleListItem(
        Long scheduleId,
        String scheduleTitle,
        boolean isRepeat,
        List<Integer> repeatDays,
        LocalDateTime appointmentAt,
        LocalDateTime nextAlarmAt,
        LocalDateTime preparationTriggeredAt,
        LocalDateTime departureTriggeredAt,
        boolean isEnabled
) {
    public static HomeScheduleListItem from(Schedule schedule) {
        return new HomeScheduleListItem(
                schedule.getId(), schedule.getTitle(), schedule.getIsRepeat(),
                schedule.getRepeatDays() == null ? List.of() : List.copyOf(schedule.getRepeatDays()),
                schedule.getAppointmentAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                schedule.getNextAlarmAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                schedule.getPreparationAlarm().getTriggeredAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                schedule.getDepartureAlarm().getTriggeredAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                schedule.getDepartureAlarm().isEnabled()
        );
    }
}
