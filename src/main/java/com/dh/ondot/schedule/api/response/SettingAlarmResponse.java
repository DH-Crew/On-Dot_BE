package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Alarm;

public record SettingAlarmResponse(
        AlarmDto preparationAlarm,
        AlarmDto departureAlarm
) {
    public static SettingAlarmResponse from(Alarm preparation, Alarm departure) {
        return new SettingAlarmResponse(
                AlarmDto.of(preparation),
                AlarmDto.of(departure)
        );
    }
}
