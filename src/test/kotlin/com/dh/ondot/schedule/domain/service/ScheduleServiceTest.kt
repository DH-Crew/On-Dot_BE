package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import com.dh.ondot.schedule.fixture.MemberFixture
import com.dh.ondot.schedule.fixture.ScheduleFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("ScheduleService 테스트")
class ScheduleServiceTest {

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @InjectMocks
    private lateinit var scheduleService: ScheduleService

    @Test
    @DisplayName("첫 번째 스케줄 설정 시 기본 설정으로 생성한다")
    fun setupSchedule_FirstTime_CreatesWithDefaultSettings() {
        // given
        val member = MemberFixture.defaultMember()
        val appointmentAt = LocalDateTime.of(2025, 12, 16, 10, 0)
        val estimatedTimeMin = 30

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id))
            .willReturn(Optional.empty())

        // when
        val result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.title).isEqualTo("새로운 일정")
        assertThat(result.isRepeat).isFalse()
        assertThat(result.appointmentAt).isNotNull()
    }

    @Test
    @DisplayName("기존 스케줄이 있는 경우 최근 설정을 복사해서 생성한다")
    fun setupSchedule_WithExistingSchedule_CopiesFromLatestSetting() {
        // given
        val member = MemberFixture.defaultMember()
        val appointmentAt = LocalDateTime.of(2025, 12, 16, 14, 0)
        val estimatedTimeMin = 30
        val latestSchedule = ScheduleFixture.defaultSchedule()

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id))
            .willReturn(Optional.of(latestSchedule))

        // when
        val result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.title).isEqualTo("새로운 일정")
        assertThat(result.preparationAlarm!!.mode)
            .isEqualTo(latestSchedule.preparationAlarm!!.mode)
        assertThat(result.departureAlarm!!.mode)
            .isEqualTo(latestSchedule.departureAlarm!!.mode)
    }

    @Test
    @DisplayName("기존 스케줄의 알람이 꺼져있어도 새 스케줄의 알람은 활성화 상태로 생성한다")
    fun setupSchedule_WithDisabledAlarms_CreatesWithEnabledAlarms() {
        // given
        val member = MemberFixture.defaultMember()
        val appointmentAt = LocalDateTime.of(2025, 12, 16, 14, 0)
        val estimatedTimeMin = 30
        val latestSchedule = ScheduleFixture.builder().disabledAlarms().build()

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id))
            .willReturn(Optional.of(latestSchedule))

        // when
        val result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin)

        // then
        assertThat(result.preparationAlarm!!.isEnabled).isTrue()
        assertThat(result.departureAlarm!!.isEnabled).isTrue()
    }

    @Test
    @DisplayName("활성 알람이 없는 스케줄들만 있는 경우 null을 반환한다")
    fun getEarliestActiveAlarmAt_NoActiveAlarms_ReturnsNull() {
        // given
        val schedule1 = ScheduleFixture.builder().disabledAlarms().build()
        val schedule2 = ScheduleFixture.builder().disabledAlarms().build()
        val schedules = listOf(schedule1, schedule2)

        // when
        val result = scheduleService.getEarliestActiveAlarmAt(schedules)

        // then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("단일 스케줄의 활성 알람 시간을 반환한다")
    fun getEarliestActiveAlarmAt_SingleSchedule_ReturnsAlarmTime() {
        // given
        val futureTime = LocalDateTime.now().plusDays(7).withHour(18).withMinute(0)
        val schedule = ScheduleFixture.builder()
            .onlyPreparationAlarmEnabled()
            .appointmentAt(futureTime)
            .build()
        val schedules = listOf(schedule)

        // when
        val result = scheduleService.getEarliestActiveAlarmAt(schedules)

        // then
        val expected = schedule.preparationAlarm!!.triggeredAt
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("여러 스케줄 중 가장 빠른 활성 알람 시간을 반환한다")
    fun getEarliestActiveAlarmAt_MultipleSchedules_ReturnsEarliest() {
        // given
        val earlierTime = LocalDateTime.now().plusDays(5).withHour(17).withMinute(0)
        val laterTime = LocalDateTime.now().plusDays(7).withHour(19).withMinute(0)

        val earlierSchedule = ScheduleFixture.builder()
            .onlyPreparationAlarmEnabled()
            .appointmentAt(earlierTime)
            .build()
        val laterSchedule = ScheduleFixture.builder()
            .onlyDepartureAlarmEnabled()
            .appointmentAt(laterTime)
            .build()

        val schedules = listOf(laterSchedule, earlierSchedule)

        // when
        val result = scheduleService.getEarliestActiveAlarmAt(schedules)

        // then
        val expected = earlierSchedule.preparationAlarm!!.triggeredAt
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("빈 스케줄 리스트의 경우 null을 반환한다")
    fun getEarliestActiveAlarmAt_EmptyList_ReturnsNull() {
        // given
        val schedules = listOf<Schedule>()

        // when
        val result = scheduleService.getEarliestActiveAlarmAt(schedules)

        // then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("스케줄을 저장한다")
    fun saveSchedule_ValidSchedule_SavesSuccessfully() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()
        given(scheduleRepository.save(any(Schedule::class.java))).willReturn(schedule)

        // when
        val result = scheduleService.saveSchedule(schedule)

        // then
        assertThat(result).isEqualTo(schedule)
        verify(scheduleRepository).save(schedule)
    }

    @Test
    @DisplayName("스케줄을 삭제한다")
    fun deleteSchedule_ValidSchedule_DeletesSuccessfully() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()

        // when
        scheduleService.deleteSchedule(schedule)

        // then
        verify(scheduleRepository).delete(schedule)
    }

    @Test
    @DisplayName("createScheduleWithAlarms는 알람만 설정하고 quick schedule 메타데이터를 설정하지 않는다")
    fun createScheduleWithAlarms_ReturnsScheduleWithAlarmsOnly() {
        // given
        val member = MemberFixture.defaultMember()
        val appointmentAt = LocalDateTime.of(2025, 12, 16, 10, 0)
        val estimatedTimeMin = 30

        given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id))
            .willReturn(Optional.empty())

        // when
        val result = scheduleService.createScheduleWithAlarms(member, appointmentAt, estimatedTimeMin)

        // then
        assertThat(result.preparationAlarm).isNotNull()
        assertThat(result.departureAlarm).isNotNull()
        assertThat(result.memberId).isEqualTo(0L)
        assertThat(result.title).isEqualTo("")
    }
}
