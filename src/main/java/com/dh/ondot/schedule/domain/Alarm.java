package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
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

    @Column(name = "delay_time", nullable = false)
    private Integer delayTime;

    @Embedded
    private Sound sound;

    @Column(name = "is_vibration", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isVibration;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission")
    private Mission mission;
}
