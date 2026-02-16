package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Schedule
import java.time.LocalDateTime

data class AlarmDto(
    val alarmId: Long?,
    val alarmMode: String,
    val isEnabled: Boolean,
    val triggeredAt: LocalDateTime?,
    val isSnoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val snoozeCount: Int,
    val soundCategory: String,
    val ringTone: String,
    val volume: Double,
) {
    companion object {
        @JvmStatic
        fun of(alarm: Alarm): AlarmDto {
            return AlarmDto(
                alarmId = alarm.id,
                alarmMode = alarm.mode.name,
                isEnabled = alarm.isEnabled,
                triggeredAt = TimeUtils.toSeoulDateTime(alarm.triggeredAt),
                isSnoozeEnabled = alarm.snooze.isSnoozeEnabled,
                snoozeInterval = alarm.snooze.snoozeInterval.value,
                snoozeCount = alarm.snooze.snoozeCount.value,
                soundCategory = alarm.sound.soundCategory.name,
                ringTone = alarm.sound.ringTone.name,
                volume = alarm.sound.volume,
            )
        }

        @JvmStatic
        fun of(alarm: Alarm, schedule: Schedule): AlarmDto {
            return AlarmDto(
                alarmId = alarm.id,
                alarmMode = alarm.mode.name,
                isEnabled = alarm.isEnabled,
                triggeredAt = TimeUtils.toSeoulDateTime(schedule.getNextRepeatAlarmTime(alarm.triggeredAt)),
                isSnoozeEnabled = alarm.snooze.isSnoozeEnabled,
                snoozeInterval = alarm.snooze.snoozeInterval.value,
                snoozeCount = alarm.snooze.snoozeCount.value,
                soundCategory = alarm.sound.soundCategory.name,
                ringTone = alarm.sound.ringTone.name,
                volume = alarm.sound.volume,
            )
        }
    }
}
