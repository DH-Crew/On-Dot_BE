package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.api.request.PlaceDto;
import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleDetailResponse(
        Long scheduleId,
        String title,
        boolean isRepeat,
        List<Integer> repeatDays,
        LocalDateTime appointmentAt,
        AlarmDto preparationAlarm,
        AlarmDto departureAlarm,
        PlaceDto departurePlace,
        PlaceDto arrivalPlace
) {
    public static ScheduleDetailResponse from(Schedule s) {
        return new ScheduleDetailResponse(
                s.getId(), s.getTitle(), s.getIsRepeat(),
                s.getRepeatDays() == null ? List.of() : List.copyOf(s.getRepeatDays()),
                DateTimeUtils.toSeoulDateTime(s.getAppointmentAt()),
                AlarmDto.of(s.getPreparationAlarm()),
                AlarmDto.of(s.getDepartureAlarm()),
                PlaceDto.from(s.getDeparturePlace()),
                PlaceDto.from(s.getArrivalPlace())
        );
    }
}
