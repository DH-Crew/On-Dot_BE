package com.dh.ondot.schedule.fixture;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;

import java.time.LocalDateTime;

public class AlarmFixture {

    public static Alarm defaultPreparationAlarm() {
        return enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0)); // 1시간 전
    }

    public static Alarm defaultDepartureAlarm() {
        return enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 30)); // 30분 전
    }

    public static Alarm enabledAlarm() {
        return enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0));
    }

    public static Alarm enabledAlarm(LocalDateTime triggeredAt) {
        return new Alarm(
                0L,
                AlarmMode.SOUND,
                true,
                TimeUtils.toInstant(triggeredAt),
                defaultSnooze(),
                defaultSound()
        );
    }

    public static Alarm disabledAlarm() {
        return disabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0));
    }

    public static Alarm disabledAlarm(LocalDateTime triggeredAt) {
        return new Alarm(
                0L,
                AlarmMode.SOUND,
                false,
                TimeUtils.toInstant(triggeredAt),
                defaultSnooze(),
                defaultSound()
        );
    }

    private static Snooze defaultSnooze() {
        return Snooze.of(true, 5, 3);
    }

    private static Sound defaultSound() {
        return Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5);
    }
}
