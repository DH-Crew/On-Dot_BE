package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.BaseTimeEntity;
import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "alarms")
public class Alarm extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private AlarmMode mode;

    @Column(name = "is_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isEnabled;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "mission")
//    private Mission mission;

    @Embedded
    private Snooze snooze;

    @Embedded
    private Sound sound;

    public static Alarm createPreparationAlarm(String alarmMode, boolean isEnabled, LocalDateTime triggeredAt,
                                               boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
                                               String soundCategory, String ringTone, Double volume) {
        return Alarm.builder()
                .mode(AlarmMode.from(alarmMode))
                .isEnabled(isEnabled)
                .triggeredAt(TimeUtils.toInstant(triggeredAt))
//                .mission(Mission.from(mission))
                .snooze(Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount))
                .sound(Sound.of(soundCategory, ringTone, volume))
                .build();
    }

    public static Alarm createDepartureAlarm(String alarmMode, LocalDateTime triggeredAt,
                                             boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
                                             String soundCategory, String ringTone, Double volume) {
        return Alarm.builder()
                .mode(AlarmMode.from(alarmMode))
                .isEnabled(true)
                .triggeredAt(TimeUtils.toInstant(triggeredAt))
                .snooze(Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount))
                .sound(Sound.of(soundCategory, ringTone, volume))
                .build();
    }

    public static Alarm createPreparationAlarmWithDefaultSetting(
            AlarmMode alarmMode, Snooze snooze, Sound sound,
            LocalDateTime appointmentAt, Integer estimatedTime, Integer preparationTime
    ) {
        return Alarm.builder()
                .mode(alarmMode)
                .isEnabled(true)
                .triggeredAt(TimeUtils.toInstant(appointmentAt.minusMinutes(estimatedTime + preparationTime)))
                .snooze(snooze)
                .sound(sound)
                .build();
    }

    public static Alarm createDepartureAlarmWithDefaultSetting(
            AlarmMode alarmMode, Snooze snooze, Sound sound,
            LocalDateTime appointmentAt, Integer estimatedTime
    ) {
        return Alarm.builder()
                .mode(alarmMode)
                .isEnabled(true)
                .triggeredAt(TimeUtils.toInstant(appointmentAt.minusMinutes(estimatedTime)))
                .snooze(snooze)
                .sound(sound)
                .build();
    }

    public void updatePreparation(
            String alarmMode, boolean isEnabled, LocalDateTime triggeredAt,
            boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
            String soundCategory, String ringTone, Double volume
    ) {
        this.mode = AlarmMode.from(alarmMode);
        this.isEnabled = isEnabled;
        this.triggeredAt = TimeUtils.toInstant(triggeredAt);
//        this.mission = Mission.from(mission);
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount);
        this.sound = Sound.of(soundCategory, ringTone, volume);
    }

    public void updateDeparture(
            String alarmMode, LocalDateTime triggeredAt,
            boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
            String soundCategory, String ringTone, Double volume
    ) {
        this.mode = AlarmMode.from(alarmMode);
        this.triggeredAt = TimeUtils.toInstant(triggeredAt);
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount);
        this.sound = Sound.of(soundCategory, ringTone, volume);
    }

    public void updateTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = TimeUtils.toInstant(triggeredAt);
    }

    public void changeEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public Alarm copy() {
        return Alarm.builder()
                .mode(this.mode)
                .isEnabled(this.isEnabled)
                .triggeredAt(this.triggeredAt)
                .snooze(this.snooze)
                .sound(this.sound)
                .build();
    }
}
