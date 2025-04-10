package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.schedule.infra.RepeatDaysConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.SortedSet;

@AggregateRoot
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "schedules")
public class Schedule extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "departure_place_id", nullable = false)
    private Long departurePlaceId;

    @Column(name = "arrival_place_id", nullable = false)
    private Long arrivalPlaceId;

    @Column(name = "preparation_alarm_id", nullable = false)
    private Long preparationAlarmId;

    @Column(name = "departure_alarm_id", nullable = false)
    private Long departureAlarmId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "is_repeat", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isRepeat;

    @Convert(converter = RepeatDaysConverter.class)
    @Column(name = "repeat_days")
    private SortedSet<Integer> repeatDays;

    @Column(name = "appointment_at", nullable = false)
    private LocalDateTime appointmentAt;
}
