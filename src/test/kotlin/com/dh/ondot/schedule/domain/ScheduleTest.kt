package com.dh.ondot.schedule.domain

import com.dh.ondot.schedule.fixture.ScheduleFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("Schedule 도메인 테스트")
class ScheduleTest {

    @Test
    @DisplayName("활성화된 알람이 하나라도 있으면 true를 반환한다")
    fun hasAnyActiveAlarm_WithEnabledAlarms_ReturnsTrue() {
        // given
        val schedule = ScheduleFixture.builder()
            .onlyPreparationAlarmEnabled()
            .build()

        // when
        val result = schedule.hasAnyActiveAlarm()

        // then
        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("모든 알람이 비활성화되면 false를 반환한다")
    fun hasAnyActiveAlarm_WithDisabledAlarms_ReturnsFalse() {
        // given
        val schedule = ScheduleFixture.builder()
            .disabledAlarms()
            .build()

        // when
        val result = schedule.hasAnyActiveAlarm()

        // then
        assertThat(result).isFalse()
    }

    @Test
    @DisplayName("일회성 스케줄의 가장 빠른 알람 시간을 계산한다")
    fun calculateEarliestAlarmAt_OneTimeSchedule_ReturnsEarliestTime() {
        // given
        val futureTime = LocalDateTime.now().plusDays(7).withHour(18).withMinute(0)
        val schedule = ScheduleFixture.builder()
            .appointmentAt(futureTime)
            .onlyPreparationAlarmEnabled()
            .isRepeat(false)
            .build()

        // when
        val result = schedule.calculateNextAlarmAt()

        // then
        assertThat(result).isNotNull()
    }

    @Test
    @DisplayName("반복 스케줄의 다음 알람 시간을 계산한다")
    fun calculateEarliestAlarmAt_RepeatSchedule_ReturnsNextRepeatTime() {
        // given
        val weekdays = ScheduleFixture.weekdays()
        val schedule = ScheduleFixture.builder()
            .isRepeat(true)
            .repeatDays(weekdays)
            .onlyPreparationAlarmEnabled()
            .build()

        // when
        val result = schedule.calculateNextAlarmAt()

        // then
        assertThat(result).satisfiesAnyOf({ instant -> assertThat(instant).isNotNull() })
    }

    @Test
    @DisplayName("모든 알람이 비활성화된 경우 null을 반환한다")
    fun calculateEarliestAlarmAt_DisabledAlarms_ReturnsNull() {
        // given
        val schedule = ScheduleFixture.builder()
            .disabledAlarms()
            .build()

        // when
        val result = schedule.calculateNextAlarmAt()

        // then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("알람을 모두 활성화/비활성화한다")
    fun switchAlarm_TogglesAllAlarms() {
        // given
        val schedule = ScheduleFixture.builder()
            .disabledAlarms()
            .build()

        // when
        schedule.switchAlarm(true)

        // then
        assertThat(schedule.preparationAlarm!!.isEnabled).isTrue()
        assertThat(schedule.departureAlarm!!.isEnabled).isTrue()

        // when
        schedule.switchAlarm(false)

        // then
        assertThat(schedule.preparationAlarm!!.isEnabled).isFalse()
        assertThat(schedule.departureAlarm!!.isEnabled).isFalse()
    }

    @Test
    @DisplayName("약속 시간이 변경되었는지 확인한다")
    fun isAppointmentTimeChanged_DifferentTime_ReturnsTrue() {
        // given
        val originalTime = LocalDateTime.of(2025, 12, 16, 10, 0)
        val newTime = LocalDateTime.of(2025, 12, 17, 10, 0)

        val schedule = ScheduleFixture.builder()
            .appointmentAt(originalTime)
            .build()

        // when
        val result = schedule.isAppointmentTimeChanged(newTime)

        // then
        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("약속 시간이 같으면 false를 반환한다")
    fun isAppointmentTimeChanged_SameTime_ReturnsFalse() {
        // given
        val appointmentTime = LocalDateTime.of(2025, 12, 16, 10, 0)

        val schedule = ScheduleFixture.builder()
            .appointmentAt(appointmentTime)
            .build()

        // when
        val result = schedule.isAppointmentTimeChanged(appointmentTime)

        // then
        assertThat(result).isFalse()
    }

    @Test
    @DisplayName("스케줄 핵심 정보를 업데이트한다")
    fun updateCore_UpdatesScheduleFields() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()
        val newTitle = "변경된 제목"
        val newIsRepeat = true
        val newRepeatDays = ScheduleFixture.weekends()
        val newAppointmentAt = LocalDateTime.of(2025, 12, 18, 10, 0)

        // when
        schedule.updateCore(newTitle, newIsRepeat, newRepeatDays, newAppointmentAt)

        // then
        assertThat(schedule.title).isEqualTo(newTitle)
        assertThat(schedule.isRepeat).isEqualTo(newIsRepeat)
        assertThat(schedule.repeatDays).isEqualTo(newRepeatDays)
    }

    @Test
    @DisplayName("일회성 스케줄 업데이트 시 반복 요일을 null로 설정한다")
    fun updateCore_OneTimeSchedule_SetsRepeatDaysToNull() {
        // given
        val schedule = ScheduleFixture.repeatSchedule(ScheduleFixture.weekdays())
        val newTitle = "일회성 스케줄"
        val newAppointmentAt = LocalDateTime.of(2025, 12, 16, 10, 0)

        // when
        schedule.updateCore(newTitle, false, null, newAppointmentAt)

        // then
        assertThat(schedule.isRepeat).isFalse()
        assertThat(schedule.repeatDays).isNull()
    }

    @Test
    @DisplayName("퀵 스케줄을 설정한다")
    fun setupQuickSchedule_SetsBasicInfo() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()
        val memberId = 999L
        val appointmentAt = LocalDateTime.of(2025, 12, 15, 19, 0)

        // when
        schedule.setupQuickSchedule(memberId, appointmentAt)

        // then
        assertThat(schedule.memberId).isEqualTo(memberId)
        assertThat(schedule.title).isEqualTo("새로운 일정")
        assertThat(schedule.isRepeat).isFalse()
    }
}
