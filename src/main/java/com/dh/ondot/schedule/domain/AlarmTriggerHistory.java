package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.BaseTimeEntity;
import com.dh.ondot.schedule.domain.enums.AlarmTriggerAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "alarm_trigger_histories")
public class AlarmTriggerHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "alarm_id", nullable = false)
    private Long alarmId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AlarmTriggerAction action;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    public static AlarmTriggerHistory record(
            Long alarmId,
            Long scheduleId,
            Instant triggeredAt,
            Instant respondedAt,
            String action,
            String deviceType
    ) {
        return AlarmTriggerHistory.builder()
                .alarmId(alarmId)
                .scheduleId(scheduleId)
                .triggeredAt(triggeredAt)
                .respondedAt(respondedAt)
                .action(AlarmTriggerAction.from(action))
                .deviceType(deviceType)
                .build();
    }
}
