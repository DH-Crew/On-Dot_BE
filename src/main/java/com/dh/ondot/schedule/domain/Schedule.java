package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.annotation.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.domain.converter.RepeatDaysConverter;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;
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

    @Column(name = "is_medication_required", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isMedicationRequired;

    @Column(name = "preparation_note", length = 100)
    private String preparationNote;

    public static Schedule createSchedule(
            Long memberId, Place departurePlace, Place arrivalPlace,
            Alarm preparationAlarm, Alarm departureAlarm, String title,
            Boolean isRepeat, SortedSet<Integer> repeatDays, LocalDateTime appointmentAt,
            Boolean isMedicationRequired, String preparationNote
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
                .appointmentAt(DateTimeUtils.toInstant(appointmentAt))
                .isMedicationRequired(isMedicationRequired)
                .preparationNote(preparationNote)
                .build();

        schedule.updateNextAlarmAt();
        return schedule;
    }

    public static Schedule createWithDefaultAlarmSetting(
            AlarmMode alarmMode, Snooze snooze, Sound sound,
            LocalDateTime appointmentAt, Integer estimatedTime, Integer preparationTime
    ) {
        return Schedule.builder()
                .preparationAlarm(
                        Alarm.createPreparationAlarmWithDefaultSetting(
                                alarmMode, snooze, sound,
                                appointmentAt, estimatedTime, preparationTime))
                .departureAlarm(
                        Alarm.createDepartureAlarmWithDefaultSetting(
                                alarmMode, snooze, sound,
                                appointmentAt, estimatedTime
                        )
                )
                .appointmentAt(DateTimeUtils.toInstant(appointmentAt))
                .build();
    }

    public void registerPlaces(Place departurePlace, Place arrivalPlace) {
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
    }

    public void updateCore(String title, boolean isRepeat,
                           SortedSet<Integer> repeatDays, LocalDateTime appointmentAt
    ) {
        this.title = title;
        this.isRepeat = isRepeat;
        this.repeatDays = isRepeat ? repeatDays : null;
        this.appointmentAt = DateTimeUtils.toInstant(appointmentAt);
    }

    public boolean isAppointmentTimeChanged(LocalDateTime newAppointmentAt) {
        return !this.appointmentAt.equals(DateTimeUtils.toInstant(newAppointmentAt));
    }

    public void setupQuickSchedule(Long memberId, LocalDateTime appointmentAt) {
        this.memberId = memberId;
        this.title = "새로운 일정";
        this.isRepeat = false;
        this.appointmentAt = DateTimeUtils.toInstant(appointmentAt);
    }

    public Instant computeNextAlarmAt() {
        Instant now = DateTimeUtils.nowSeoulInstant();
        
        Instant nextPreparationTime = getNextActivePreparationAlarmTime();
        Instant nextDepartureTime = getNextActiveDepartureAlarmTime();
        
        return selectEarliestActiveAlarmAfter(now, nextPreparationTime, nextDepartureTime);
    }

    public void updateNextAlarmAt() {
        this.nextAlarmAt = computeNextAlarmAt();
    }

    public void switchAlarm(boolean enabled) {
        this.preparationAlarm.changeEnabled(enabled);
        this.departureAlarm.changeEnabled(enabled);
    }

    public boolean hasAnyActiveAlarm() {
        return preparationAlarm.isEnabled() || departureAlarm.isEnabled();
    }

    private Instant getNextActivePreparationAlarmTime() {
        if (!preparationAlarm.isEnabled()) {
            return Instant.MAX;
        }
        return calculateNextAlarmTimeByRepeatSettings(preparationAlarm.getTriggeredAt());
    }

    private Instant getNextActiveDepartureAlarmTime() {
        if (!departureAlarm.isEnabled()) {
            return Instant.MAX;
        }
        return calculateNextAlarmTimeByRepeatSettings(departureAlarm.getTriggeredAt());
    }

    private Instant selectEarliestActiveAlarmAfter(Instant now, Instant preparationTime, Instant departureTime) {
        // 둘 다 비활성화된 경우
        if (preparationTime.equals(Instant.MAX) && departureTime.equals(Instant.MAX)) {
            return now;
        }
        
        // 준비 알람이 이미 지난 경우 출발 알람 반환
        if (preparationTime.isBefore(now)) {
            return departureTime.equals(Instant.MAX) ? now : departureTime;
        }
        
        // 둘 중 더 빠른 알람 반환
        return preparationTime.isBefore(departureTime) ? preparationTime : departureTime;
    }

    /**
     * 반복 설정에 따라 다음 알람 시간을 계산한다.
     * 일회성 알람: 기본 시간 그대로 반환
     * 반복 알람: 오늘부터 7일 이내에 해당 요일에 울릴 다음 시간 계산
     */
    private Instant calculateNextAlarmTimeByRepeatSettings(Instant baseAlarmTime) {
        if (!isRepeat) {
            return baseAlarmTime;
        }

        Instant now = Instant.now();
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalTime alarmTime = baseAlarmTime.atZone(seoulZone).toLocalTime();
        LocalDate today = LocalDate.now(seoulZone);

        final int DAYS_IN_WEEK = 7;

        for (int daysAhead = 0; daysAhead < DAYS_IN_WEEK; daysAhead++) {
            LocalDate candidateDate = today.plusDays(daysAhead);

            if (isScheduledForDayOfWeek(candidateDate)) {
                Instant candidateAlarmTime = DateTimeUtils.toInstant(candidateDate.atTime(alarmTime));

                if (candidateAlarmTime.isAfter(now)) {
                    return candidateAlarmTime;
                }
            }
        }

        return baseAlarmTime;
    }
    
    // 특정 날짜가 반복 요일에 해당하는지 확인한다
    private boolean isScheduledForDayOfWeek(LocalDate date) {
        // repeatDays: [1(일) .. 7(토)], DayOfWeek: [1(월) .. 7(일)]
        // 변환 로직: 월(1)->2, 화(2)->3, ..., 토(6)->7, 일(7)->1
        int dayValue = (date.getDayOfWeek().getValue() % 7) + 1;
        return repeatDays != null && repeatDays.contains(dayValue);
    }
}
