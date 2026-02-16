package com.dh.ondot.schedule.domain;

import com.dh.ondot.schedule.fixture.ScheduleFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Schedule 도메인 테스트")
class ScheduleTest {

    @Test
    @DisplayName("활성화된 알람이 하나라도 있으면 true를 반환한다")
    void hasAnyActiveAlarm_WithEnabledAlarms_ReturnsTrue() {
        // given
        Schedule schedule = ScheduleFixture.builder()
                .onlyPreparationAlarmEnabled()
                .build();

        // when
        boolean result = schedule.hasAnyActiveAlarm();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 알람이 비활성화되면 false를 반환한다")
    void hasAnyActiveAlarm_WithDisabledAlarms_ReturnsFalse() {
        // given
        Schedule schedule = ScheduleFixture.builder()
                .disabledAlarms()
                .build();

        // when
        boolean result = schedule.hasAnyActiveAlarm();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("일회성 스케줄의 가장 빠른 알람 시간을 계산한다")
    void calculateEarliestAlarmAt_OneTimeSchedule_ReturnsEarliestTime() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7).withHour(18).withMinute(0);
        Schedule schedule = ScheduleFixture.builder()
                .appointmentAt(futureTime)
                .onlyPreparationAlarmEnabled()
                .isRepeat(false)
                .build();

        // when
        Instant result = schedule.calculateNextAlarmAt();

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("반복 스케줄의 다음 알람 시간을 계산한다")
    void calculateEarliestAlarmAt_RepeatSchedule_ReturnsNextRepeatTime() {
        // given
        SortedSet<Integer> weekdays = ScheduleFixture.weekdays();
        Schedule schedule = ScheduleFixture.builder()
                .isRepeat(true)
                .repeatDays(weekdays)
                .onlyPreparationAlarmEnabled()
                .build();

        // when
        Instant result = schedule.calculateNextAlarmAt();

        // then
        assertThat(result).satisfiesAnyOf(instant -> assertThat(instant).isNotNull());
    }

    @Test
    @DisplayName("모든 알람이 비활성화된 경우 null을 반환한다")
    void calculateEarliestAlarmAt_DisabledAlarms_ReturnsNull() {
        // given
        Schedule schedule = ScheduleFixture.builder()
                .disabledAlarms()
                .build();

        // when
        Instant result = schedule.calculateNextAlarmAt();

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("알람을 모두 활성화/비활성화한다")
    void switchAlarm_TogglesAllAlarms() {
        // given
        Schedule schedule = ScheduleFixture.builder()
                .disabledAlarms()
                .build();

        // when
        schedule.switchAlarm(true);

        // then
        assertThat(schedule.getPreparationAlarm().isEnabled()).isTrue();
        assertThat(schedule.getDepartureAlarm().isEnabled()).isTrue();

        // when
        schedule.switchAlarm(false);

        // then
        assertThat(schedule.getPreparationAlarm().isEnabled()).isFalse();
        assertThat(schedule.getDepartureAlarm().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("약속 시간이 변경되었는지 확인한다")
    void isAppointmentTimeChanged_DifferentTime_ReturnsTrue() {
        // given
        LocalDateTime originalTime = LocalDateTime.of(2025, 12, 16, 10, 0);
        LocalDateTime newTime = LocalDateTime.of(2025, 12, 17, 10, 0);
        
        Schedule schedule = ScheduleFixture.builder()
                .appointmentAt(originalTime)
                .build();

        // when
        boolean result = schedule.isAppointmentTimeChanged(newTime);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("약속 시간이 같으면 false를 반환한다")
    void isAppointmentTimeChanged_SameTime_ReturnsFalse() {
        // given
        LocalDateTime appointmentTime = LocalDateTime.of(2025, 12, 16, 10, 0);
        
        Schedule schedule = ScheduleFixture.builder()
                .appointmentAt(appointmentTime)
                .build();

        // when
        boolean result = schedule.isAppointmentTimeChanged(appointmentTime);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("스케줄 핵심 정보를 업데이트한다")
    void updateCore_UpdatesScheduleFields() {
        // given
        Schedule schedule = ScheduleFixture.defaultSchedule();
        String newTitle = "변경된 제목";
        boolean newIsRepeat = true;
        SortedSet<Integer> newRepeatDays = ScheduleFixture.weekends();
        LocalDateTime newAppointmentAt = LocalDateTime.of(2025, 12, 18, 10, 0);

        // when
        schedule.updateCore(newTitle, newIsRepeat, newRepeatDays, newAppointmentAt);

        // then
        assertThat(schedule.getTitle()).isEqualTo(newTitle);
        assertThat(schedule.isRepeat()).isEqualTo(newIsRepeat);
        assertThat(schedule.getRepeatDays()).isEqualTo(newRepeatDays);
    }

    @Test
    @DisplayName("일회성 스케줄 업데이트 시 반복 요일을 null로 설정한다")
    void updateCore_OneTimeSchedule_SetsRepeatDaysToNull() {
        // given
        Schedule schedule = ScheduleFixture.repeatSchedule(ScheduleFixture.weekdays());
        String newTitle = "일회성 스케줄";
        LocalDateTime newAppointmentAt = LocalDateTime.of(2025, 12, 16, 10, 0);

        // when
        schedule.updateCore(newTitle, false, null, newAppointmentAt);

        // then
        assertThat(schedule.isRepeat()).isFalse();
        assertThat(schedule.getRepeatDays()).isNull();
    }

    @Test
    @DisplayName("퀵 스케줄을 설정한다")
    void setupQuickSchedule_SetsBasicInfo() {
        // given
        Schedule schedule = ScheduleFixture.defaultSchedule();
        Long memberId = 999L;
        LocalDateTime appointmentAt = LocalDateTime.of(2025, 12, 15, 19, 0);

        // when
        schedule.setupQuickSchedule(memberId, appointmentAt);

        // then
        assertThat(schedule.getMemberId()).isEqualTo(memberId);
        assertThat(schedule.getTitle()).isEqualTo("새로운 일정");
        assertThat(schedule.isRepeat()).isFalse();
    }
}
