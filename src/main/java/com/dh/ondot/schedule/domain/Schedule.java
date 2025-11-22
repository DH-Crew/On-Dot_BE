package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.BaseTimeEntity;
import com.dh.ondot.core.util.TimeUtils;
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
        return Schedule.builder()
                .memberId(memberId)
                .departurePlace(departurePlace)
                .arrivalPlace(arrivalPlace)
                .preparationAlarm(preparationAlarm)
                .departureAlarm(departureAlarm)
                .title(title)
                .isRepeat(isRepeat)
                .repeatDays(isRepeat ? repeatDays : null)
                .appointmentAt(TimeUtils.toInstant(appointmentAt))
                .isMedicationRequired(isMedicationRequired)
                .preparationNote(preparationNote)
                .build();
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
                .appointmentAt(TimeUtils.toInstant(appointmentAt))
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
        this.appointmentAt = TimeUtils.toInstant(appointmentAt);
    }

    public boolean isAppointmentTimeChanged(LocalDateTime newAppointmentAt) {
        return !this.appointmentAt.equals(TimeUtils.toInstant(newAppointmentAt));
    }

    public void setupQuickSchedule(Long memberId, LocalDateTime appointmentAt) {
        this.memberId = memberId;
        this.title = "새로운 일정";
        this.isRepeat = false;
        this.appointmentAt = TimeUtils.toInstant(appointmentAt);
    }

    public void switchAlarm(boolean enabled) {
        this.preparationAlarm.changeEnabled(enabled);
        this.departureAlarm.changeEnabled(enabled);
    }

    public boolean hasAnyActiveAlarm() {
        return preparationAlarm.isEnabled() || departureAlarm.isEnabled();
    }

    /**
     * 반복 설정에 따라 다음 알람 시간을 계산한다
     * hasAnyActiveAlarm이 true인 경우 사용한다
     * 일회성 알람: preparationAlarm과 departureAlarm 중 활성화된 것 중 현재 시간 이후 가장 빠른 것 반환
     * 반복 알람: repeatDays를 고려해서 현재 시간 이후 가장 먼저 울릴 알람 시간 계산
     */
    public Instant calculateNextAlarmAt() {
        if (!isRepeat) {
            Instant prepAlarmAt = preparationAlarm.isEnabled() ? preparationAlarm.getTriggeredAt() : null;
            Instant deptAlarmAt = departureAlarm.isEnabled() ? departureAlarm.getTriggeredAt() : null;
            return TimeUtils.findEarliestAfterNow(prepAlarmAt, deptAlarmAt);
        }

        // 반복 일정 처리
        Instant nextPrepAlarmAt = preparationAlarm.isEnabled()
            ? calculateNextRepeatTime(preparationAlarm.getTriggeredAt()) 
            : null;
            
        Instant nextDeptAlarmAt = departureAlarm.isEnabled()
            ? calculateNextRepeatTime(departureAlarm.getTriggeredAt()) 
            : null;
            
        return TimeUtils.findEarliestAfterNow(nextPrepAlarmAt, nextDeptAlarmAt);
    }

    public Instant getNextRepeatAlarmTime(Instant baseAlarmTime) {
        if (!isRepeat) {
            return baseAlarmTime;
        }
        return calculateNextRepeatTime(baseAlarmTime);
    }

    /**
     * 반복 설정에 따라 다음 알람 시간을 계산한다.
     * 현재 시간 이후 7일 이내에서 해당 요일에 맞는 가장 빠른 시간을 찾는다.
     */
    private Instant calculateNextRepeatTime(Instant baseAlarmTime) {
        Instant now = Instant.now();
        LocalTime alarmTime = TimeUtils.toSeoulTime(baseAlarmTime);
        LocalDate today = TimeUtils.nowSeoulDate();
        
        for (int daysAhead = 0; daysAhead <= 7; daysAhead++) {
            LocalDate candidateDate = today.plusDays(daysAhead);
            
            if (isScheduledForDayOfWeek(candidateDate)) {
                Instant candidateTime = TimeUtils.toInstant(candidateDate.atTime(alarmTime));
                
                if (candidateTime.isAfter(now)) {
                    return candidateTime;
                }
            }
        }
        
        return null;
    }
    
    // 특정 날짜가 반복 요일에 해당하는지 확인한다
    private boolean isScheduledForDayOfWeek(LocalDate date) {
        // repeatDays: [1(일) .. 7(토)], DayOfWeek: [1(월) .. 7(일)]
        // 변환 로직: 월(1)->2, 화(2)->3, ..., 토(6)->7, 일(7)->1
        int dayValue = (date.getDayOfWeek().getValue() % 7) + 1;
        return repeatDays.contains(dayValue);
    }
}
