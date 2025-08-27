package com.dh.ondot.schedule.fixture;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;

import java.time.LocalDateTime;

public class AlarmFixture {

    public static Alarm defaultPreparationAlarm() {
        return enabledAlarm(LocalDateTime.now().plusHours(1));
    }

    public static Alarm defaultDepartureAlarm() {
        return enabledAlarm(LocalDateTime.now().plusMinutes(90));
    }

    public static Alarm enabledAlarm() {
        return enabledAlarm(LocalDateTime.now().plusHours(1));
    }

    public static Alarm enabledAlarm(LocalDateTime triggeredAt) {
        return Alarm.builder()
                .mode(AlarmMode.SOUND)
                .isEnabled(true)
                .triggeredAt(DateTimeUtils.toInstant(triggeredAt))
                .snooze(defaultSnooze())
                .sound(defaultSound())
                .build();
    }

    public static Alarm disabledAlarm() {
        return disabledAlarm(LocalDateTime.now().plusHours(1));
    }

    public static Alarm disabledAlarm(LocalDateTime triggeredAt) {
        return Alarm.builder()
                .mode(AlarmMode.SOUND)
                .isEnabled(false)
                .triggeredAt(DateTimeUtils.toInstant(triggeredAt))
                .snooze(defaultSnooze())
                .sound(defaultSound())
                .build();
    }

    private static Snooze defaultSnooze() {
        return Snooze.of(true, 5, 3);
    }

    private static Sound defaultSound() {
        return Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5);
    }
}
