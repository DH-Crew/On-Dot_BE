package com.dh.ondot.schedule.application.dto;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.api.response.AlarmDto;
import com.dh.ondot.schedule.domain.Schedule;

import java.time.*;
import java.util.List;

public record HomeScheduleListItem(
        Long scheduleId,
        Double startLongitude,
        Double startLatitude,
        Double endLongitude,
        Double endLatitude,
        String scheduleTitle,
        boolean isRepeat,
        List<Integer> repeatDays,
        LocalDateTime appointmentAt,
        AlarmDto preparationAlarm,
        AlarmDto departureAlarm,
        LocalDateTime nextAlarmAt,
        boolean hasActiveAlarm
) {
    public static HomeScheduleListItem from(Schedule schedule) {
        return new HomeScheduleListItem(
                schedule.getId(),
                schedule.getDeparturePlace().getLongitude(),
                schedule.getDeparturePlace().getLatitude(),
                schedule.getArrivalPlace().getLongitude(),
                schedule.getArrivalPlace().getLatitude(),
                schedule.getTitle(),
                schedule.getIsRepeat(),
                schedule.getRepeatDays() == null ? List.of() : List.copyOf(schedule.getRepeatDays()),
                DateTimeUtils.toSeoulDateTime(schedule.getAppointmentAt()),
                AlarmDto.of(schedule.getPreparationAlarm()),
                AlarmDto.of(schedule.getDepartureAlarm()),
                DateTimeUtils.toSeoulDateTime(schedule.getNextAlarmAt()),
                schedule.hasAnyActiveAlarm()
        );
    }
}
