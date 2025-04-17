package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Alarm;

public record LatestAlarmResponse(
        AlarmDto preparationAlarm,
        AlarmDto departureAlarm
) {
    public static LatestAlarmResponse from(Alarm preparation, Alarm departure) {
        return new LatestAlarmResponse(
                AlarmDto.of(preparation),
                AlarmDto.of(departure)
        );
    }
}
