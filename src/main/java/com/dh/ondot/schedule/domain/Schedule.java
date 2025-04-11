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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "departure_place_id", nullable = false)
    private Place departurePlace;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "arrival_place_id", nullable = false)
    private Place arrivalPlace;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "preparation_alarm_id", nullable = false)
    private Alarm preparationAlarm;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "departure_alarm_id", nullable = false)
    private Alarm departureAlarm;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "is_repeat", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isRepeat;

    @Convert(converter = RepeatDaysConverter.class)
    @Column(name = "repeat_days")
    private SortedSet<Integer> repeatDays;

    @Column(name = "appointment_at", nullable = false)
    private LocalDateTime appointmentAt;

    public static Schedule createSchedule(Long memberId, Place departurePlace, Place arrivalPlace,
                                          Alarm preparationAlarm, Alarm departureAlarm, String title,
                                          Boolean isRepeat, SortedSet<Integer> repeatDays, LocalDateTime appointmentAt
    ) {
        return Schedule.builder()
                .memberId(memberId)
                .departurePlace(departurePlace)
                .arrivalPlace(arrivalPlace)
                .preparationAlarm(preparationAlarm)
                .departureAlarm(departureAlarm)
                .title(title)
                .isRepeat(isRepeat)
                .repeatDays(isRepeat ? repeatDays : null)
                .appointmentAt(appointmentAt)
                .build();
    }
}
