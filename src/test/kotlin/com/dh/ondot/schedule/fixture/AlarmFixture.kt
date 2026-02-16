package com.dh.ondot.schedule.fixture

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import java.time.LocalDateTime

object AlarmFixture {

    @JvmStatic
    fun defaultPreparationAlarm(): Alarm =
        enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0)) // 1시간 전

    @JvmStatic
    fun defaultDepartureAlarm(): Alarm =
        enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 30)) // 30분 전

    @JvmStatic
    fun enabledAlarm(): Alarm =
        enabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0))

    @JvmStatic
    fun enabledAlarm(triggeredAt: LocalDateTime): Alarm = Alarm(
        0L,
        AlarmMode.SOUND,
        true,
        TimeUtils.toInstant(triggeredAt),
        defaultSnooze(),
        defaultSound()
    )

    @JvmStatic
    fun disabledAlarm(): Alarm =
        disabledAlarm(LocalDateTime.of(2025, 12, 15, 15, 0))

    @JvmStatic
    fun disabledAlarm(triggeredAt: LocalDateTime): Alarm = Alarm(
        0L,
        AlarmMode.SOUND,
        false,
        TimeUtils.toInstant(triggeredAt),
        defaultSnooze(),
        defaultSound()
    )

    private fun defaultSnooze(): Snooze = Snooze.of(true, 5, 3)

    private fun defaultSound(): Sound = Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5)
}
