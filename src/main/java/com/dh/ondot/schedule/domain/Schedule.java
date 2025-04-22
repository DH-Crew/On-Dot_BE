package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.schedule.domain.converter.RepeatDaysConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
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
    private Instant appointmentAt;

    @Column(name = "next_alarm_at")
    private Instant nextAlarmAt;

    public static Schedule createSchedule(Long memberId, Place departurePlace, Place arrivalPlace,
                                          Alarm preparationAlarm, Alarm departureAlarm, String title,
                                          Boolean isRepeat, SortedSet<Integer> repeatDays, LocalDateTime appointmentAt
    ) {
        Schedule schedule = Schedule.builder()
                .memberId(memberId)
                .departurePlace(departurePlace)
                .arrivalPlace(arrivalPlace)
                .preparationAlarm(preparationAlarm)
                .departureAlarm(departureAlarm)
                .title(title)
                .isRepeat(isRepeat)
                .repeatDays(isRepeat ? repeatDays : null)
                .appointmentAt(appointmentAt.atZone(ZoneId.of("Asia/Seoul")).toInstant())
                .build();

        schedule.updateNextAlarmAt();
        return schedule;
    }

    public void updateCore(String title, boolean isRepeat,
                           SortedSet<Integer> repeatDays, LocalDateTime appointmentAt
    ) {
        this.title         = title;
        this.isRepeat      = isRepeat;
        this.repeatDays    = isRepeat ? repeatDays : null;
        this.appointmentAt = appointmentAt.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    public boolean isAppointmentTimeChanged(LocalDateTime newAppointmentAt) {
        return !this.appointmentAt.equals(newAppointmentAt.atZone(ZoneId.of("Asia/Seoul")).toInstant());
    }

    public void updateNextAlarmAt() {
        Instant preparationTriggeredAt = this.preparationAlarm.getTriggeredAt();
        Instant departureTriggeredAt = this.departureAlarm.getTriggeredAt();

        Instant preparationTime = calculateNextTriggeredAt(preparationTriggeredAt);
        Instant departureTime = calculateNextTriggeredAt(departureTriggeredAt);

        this.nextAlarmAt = preparationTime.isBefore(departureTime)? preparationTime : departureTime;
    }

    public void switchAlarm(boolean enabled) {
        this.departureAlarm.changeEnabled(enabled);
    }

    /**
     * 반복 여부에 따라 “다음에 실제로 울릴 시각”을 계산한다
     * 일회성 알람이면, base 그대로
     * 반복 알람이면, today ~ today+6 사이에 repeatDay에 해당하고 now 이후인 첫 시각
     * @param base  저장된 알람 시간(Instant)
     * @return      앞으로 7 일 이내에 가장 빠르게 작동하는 알람 시간(Instant)
     */
    private Instant calculateNextTriggeredAt(Instant base) {
        Instant now = Instant.now();
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalTime alarmTime = base.atZone(zone).toLocalTime();
        LocalDate today = LocalDate.now(zone);

        if (!isRepeat) {
            return base;
        }

        /* 반복 알람: 앞으로 7 일 탐색
         * today(+0) ~ today(+6) 를 돌면서
         * 요일이 repeatDay 에 포함되고 지금(now) 이후인 첫 시간을 찾는다
         */
        for (int plus = 0; plus < 7; plus++) {
            LocalDate candidateDate = today.plusDays(plus);
            int myDayValue = (candidateDate.getDayOfWeek().getValue() % 7) + 1;

            // repeatDays : [1(Sun) .. 7(Sat)]
            if (repeatDays.contains(myDayValue)) {
                LocalDateTime candidateDateTime = candidateDate.atTime(alarmTime);
                Instant candidateInstant = candidateDateTime.atZone(zone).toInstant();

                if (candidateInstant.isAfter(now)) {
                    return candidateInstant;
                }
            }
        }
        return base;
    }
}
