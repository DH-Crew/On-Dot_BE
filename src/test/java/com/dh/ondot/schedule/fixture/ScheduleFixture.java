package com.dh.ondot.schedule.fixture;

import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

public class ScheduleFixture {

    public static ScheduleBuilder builder() {
        return new ScheduleBuilder();
    }

    public static Schedule defaultSchedule() {
        return builder().build();
    }

    public static Schedule repeatSchedule(SortedSet<Integer> repeatDays) {
        return builder()
                .isRepeat(true)
                .repeatDays(repeatDays)
                .build();
    }

    public static class ScheduleBuilder {
        private Long memberId = 1L;
        private String title = "테스트 일정";
        private Boolean isRepeat = false;
        private SortedSet<Integer> repeatDays = null;
        private LocalDateTime appointmentAt = LocalDateTime.now().plusHours(2);
        private Boolean isMedicationRequired = false;
        private String preparationNote = "준비 메모";
        private Place departurePlace = PlaceFixture.defaultDeparturePlace();
        private Place arrivalPlace = PlaceFixture.defaultArrivalPlace();
        private Alarm preparationAlarm = AlarmFixture.defaultPreparationAlarm();
        private Alarm departureAlarm = AlarmFixture.defaultDepartureAlarm();

        public ScheduleBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public ScheduleBuilder isRepeat(Boolean isRepeat) {
            this.isRepeat = isRepeat;
            return this;
        }

        public ScheduleBuilder repeatDays(SortedSet<Integer> repeatDays) {
            this.repeatDays = repeatDays;
            return this;
        }

        public ScheduleBuilder appointmentAt(LocalDateTime appointmentAt) {
            this.appointmentAt = appointmentAt;
            return this;
        }

        public ScheduleBuilder disabledAlarms() {
            this.preparationAlarm = AlarmFixture.disabledAlarm();
            this.departureAlarm = AlarmFixture.disabledAlarm();
            return this;
        }

        public ScheduleBuilder onlyPreparationAlarmEnabled() {
            this.preparationAlarm = AlarmFixture.enabledAlarm();
            this.departureAlarm = AlarmFixture.disabledAlarm();
            return this;
        }

        public ScheduleBuilder onlyDepartureAlarmEnabled() {
            this.preparationAlarm = AlarmFixture.disabledAlarm();
            this.departureAlarm = AlarmFixture.enabledAlarm();
            return this;
        }

        public Schedule build() {
            return Schedule.createSchedule(
                    memberId, departurePlace, arrivalPlace,
                    preparationAlarm, departureAlarm, title,
                    isRepeat, repeatDays, appointmentAt,
                    isMedicationRequired, preparationNote
            );
        }
    }

    public static SortedSet<Integer> weekdays() {
        SortedSet<Integer> days = new TreeSet<>();
        days.add(2); // 월
        days.add(3); // 화
        days.add(4); // 수
        days.add(5); // 목
        days.add(6); // 금
        return days;
    }

    public static SortedSet<Integer> weekends() {
        SortedSet<Integer> days = new TreeSet<>();
        days.add(1); // 일
        days.add(7); // 토
        return days;
    }
}
