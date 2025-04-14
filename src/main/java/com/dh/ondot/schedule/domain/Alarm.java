package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.enums.Mission;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;
import jakarta.persistence.*;
import lombok.*;

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
    private LocalDateTime triggeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission")
    private Mission mission;

    @Embedded
    private Snooze snooze;

    @Embedded
    private Sound sound;

    public static Alarm createPreparationAlarm(String alarmMode, boolean isEnabled, LocalDateTime triggeredAt, String mission,
                                               boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
                                               String soundCategory, String ringTone, Integer volume) {
        return Alarm.builder()
                .mode(AlarmMode.from(alarmMode))
                .isEnabled(isEnabled)
                .triggeredAt(triggeredAt)
                .mission(Mission.from(mission))
                .snooze(Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount))
                .sound(Sound.of(soundCategory, ringTone, volume))
                .build();
    }

    public static Alarm createDepartureAlarm(String alarmMode, LocalDateTime triggeredAt,
                                             boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
                                             String soundCategory, String ringTone, Integer volume) {
        return Alarm.builder()
                .mode(AlarmMode.from(alarmMode))
                .isEnabled(true)
                .triggeredAt(triggeredAt)
                .snooze(Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount))
                .sound(Sound.of(soundCategory, ringTone, volume))
                .build();
    }

    public void updatePreparation(String alarmMode, boolean isEnabled,
                                  LocalDateTime triggeredAt, String mission,
                                  boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
                                  String soundCategory, String ringTone, Integer volume
    ) {
        this.mode          = AlarmMode.from(alarmMode);
        this.isEnabled     = isEnabled;
        this.triggeredAt   = triggeredAt;
        this.mission       = Mission.from(mission);
        this.snooze        = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount);
        this.sound         = Sound.of(soundCategory, ringTone, volume);
    }

    public void updateDeparture(String alarmMode, boolean isSnoozeEnabled,
                                Integer snoozeInterval, Integer snoozeCount,
                                String soundCategory, String ringTone, Integer volume
    ) {
        this.mode        = AlarmMode.from(alarmMode);
        this.snooze      = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount);
        this.sound       = Sound.of(soundCategory, ringTone, volume);
    }
}
