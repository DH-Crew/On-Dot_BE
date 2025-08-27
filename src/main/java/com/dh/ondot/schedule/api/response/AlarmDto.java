package com.dh.ondot.schedule.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.Alarm;

import java.time.LocalDateTime;

public record AlarmDto(
        Long alarmId,
        String alarmMode,
        boolean isEnabled,
        LocalDateTime triggeredAt,
        boolean isSnoozeEnabled,
        Integer snoozeInterval,
        Integer snoozeCount,
        String soundCategory,
        String ringTone,
        Double volume
) {
    public static AlarmDto of(Alarm alarm) {
        return new AlarmDto(
                alarm.getId(),
                alarm.getMode().name(),
                alarm.isEnabled(),
                TimeUtils.toSeoulDateTime(alarm.getTriggeredAt()),
                alarm.getSnooze().isSnoozeEnabled(),
                alarm.getSnooze().getSnoozeInterval().getValue(),
                alarm.getSnooze().getSnoozeCount().getValue(),
                alarm.getSound().getSoundCategory().name(),
                alarm.getSound().getRingTone().name(),
                alarm.getSound().getVolume()
        );
    }
}
