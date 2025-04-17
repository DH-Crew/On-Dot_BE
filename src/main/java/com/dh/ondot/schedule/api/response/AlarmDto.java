package com.dh.ondot.schedule.api.response;

import com.dh.ondot.schedule.domain.Alarm;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record AlarmDto(
        String alarmMode,
        boolean isEnabled,
        LocalDateTime triggeredAt,
        boolean isSnoozeEnabled,
        Integer snoozeInterval,
        Integer snoozeCount,
        String soundCategory,
        String ringTone,
        Integer volume
) {
    public static AlarmDto of(Alarm alarm) {
        return new AlarmDto(
                alarm.getMode().name(),
                alarm.isEnabled(),
                alarm.getTriggeredAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                alarm.getSnooze().isSnoozeEnabled(),
                alarm.getSnooze().getSnoozeInterval().getValue(),
                alarm.getSnooze().getSnoozeCount().getValue(),
                alarm.getSound().getSoundCategory().name(),
                alarm.getSound().getRingTone().name(),
                alarm.getSound().getVolume()
        );
    }
}
