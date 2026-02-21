package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "alarms")
class Alarm(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    var mode: AlarmMode,

    @Column(name = "is_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    var isEnabled: Boolean,

    @Column(name = "triggered_at", nullable = false)
    var triggeredAt: Instant,

    @Embedded
    var snooze: Snooze,

    @Embedded
    var sound: Sound,
) : BaseTimeEntity() {

    protected constructor() : this(
        mode = AlarmMode.SOUND,
        isEnabled = false,
        triggeredAt = Instant.now(),
        snooze = Snooze.of(false, 5, 3),
        sound = Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5),
    )

    fun updatePreparation(
        alarmMode: String, isEnabled: Boolean, triggeredAt: LocalDateTime,
        isSnoozeEnabled: Boolean, snoozeInterval: Int, snoozeCount: Int,
        soundCategory: String, ringTone: String, volume: Double,
    ) {
        this.mode = AlarmMode.from(alarmMode)
        this.isEnabled = isEnabled
        this.triggeredAt = TimeUtils.toInstant(triggeredAt)
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount)
        this.sound = Sound.of(soundCategory, ringTone, volume)
    }

    fun updateDeparture(
        alarmMode: String, triggeredAt: LocalDateTime,
        isSnoozeEnabled: Boolean, snoozeInterval: Int, snoozeCount: Int,
        soundCategory: String, ringTone: String, volume: Double,
    ) {
        this.mode = AlarmMode.from(alarmMode)
        this.triggeredAt = TimeUtils.toInstant(triggeredAt)
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount)
        this.sound = Sound.of(soundCategory, ringTone, volume)
    }

    fun updateTriggeredAt(triggeredAt: LocalDateTime) {
        this.triggeredAt = TimeUtils.toInstant(triggeredAt)
    }

    fun changeEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    fun copy(): Alarm = Alarm(
        mode = this.mode,
        isEnabled = this.isEnabled,
        triggeredAt = this.triggeredAt,
        snooze = this.snooze,
        sound = this.sound,
    )

    companion object {
        fun createPreparationAlarm(
            alarmMode: String, isEnabled: Boolean, triggeredAt: LocalDateTime,
            isSnoozeEnabled: Boolean, snoozeInterval: Int, snoozeCount: Int,
            soundCategory: String, ringTone: String, volume: Double,
        ): Alarm = Alarm(
            mode = AlarmMode.from(alarmMode),
            isEnabled = isEnabled,
            triggeredAt = TimeUtils.toInstant(triggeredAt),
            snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount),
            sound = Sound.of(soundCategory, ringTone, volume),
        )

        fun createDepartureAlarm(
            alarmMode: String, triggeredAt: LocalDateTime,
            isSnoozeEnabled: Boolean, snoozeInterval: Int, snoozeCount: Int,
            soundCategory: String, ringTone: String, volume: Double,
        ): Alarm = Alarm(
            mode = AlarmMode.from(alarmMode),
            isEnabled = true,
            triggeredAt = TimeUtils.toInstant(triggeredAt),
            snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount),
            sound = Sound.of(soundCategory, ringTone, volume),
        )

        fun createPreparationAlarmWithDefaultSetting(
            alarmMode: AlarmMode, snooze: Snooze, sound: Sound,
            appointmentAt: LocalDateTime, estimatedTime: Int, preparationTime: Int,
        ): Alarm = Alarm(
            mode = alarmMode,
            isEnabled = true,
            triggeredAt = TimeUtils.toInstant(appointmentAt.minusMinutes((estimatedTime + preparationTime).toLong())),
            snooze = snooze,
            sound = sound,
        )

        fun createDepartureAlarmWithDefaultSetting(
            alarmMode: AlarmMode, snooze: Snooze, sound: Sound,
            appointmentAt: LocalDateTime, estimatedTime: Int,
        ): Alarm = Alarm(
            mode = alarmMode,
            isEnabled = true,
            triggeredAt = TimeUtils.toInstant(appointmentAt.minusMinutes(estimatedTime.toLong())),
            snooze = snooze,
            sound = sound,
        )
    }
}
