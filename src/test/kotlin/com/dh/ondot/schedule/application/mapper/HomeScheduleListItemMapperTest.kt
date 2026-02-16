package com.dh.ondot.schedule.application.mapper

import com.dh.ondot.schedule.application.dto.HomeScheduleListItem
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.fixture.AlarmFixture
import com.dh.ondot.schedule.fixture.PlaceFixture
import com.dh.ondot.schedule.fixture.ScheduleFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.TreeSet

@DisplayName("HomeScheduleListItemMapper 테스트")
class HomeScheduleListItemMapperTest {

    private val mapper = HomeScheduleListItemMapper()

    @Test
    @DisplayName("알람 활성화 상태에 따라 정렬한다 - ON 알람이 OFF 알람보다 우선")
    fun toListOrderedByAlarmPriority_SortsByAlarmState() {
        // given
        val futureTime1 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)
        val futureTime2 = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0)

        // 알람 OFF - 더 빠른 시간이지만 비활성화
        val disabledSchedule = ScheduleFixture.builder()
            .appointmentAt(futureTime2)
            .disabledAlarms()
            .build()

        // 알람 ON - 더 늦은 시간이지만 활성화
        val enabledSchedule = ScheduleFixture.builder()
            .appointmentAt(futureTime1)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(disabledSchedule, enabledSchedule)

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        // 1순위: 알람 활성화 상태 (ON > OFF)
        assertThat(result).hasSize(2)
        assertThat(result[0].hasActiveAlarm).isTrue()  // enabledSchedule
        assertThat(result[1].hasActiveAlarm).isFalse() // disabledSchedule
    }

    @Test
    @DisplayName("동일한 활성화 상태에서는 다음 알람 시간 순으로 정렬한다")
    fun toListOrderedByAlarmPriority_SortsByNextAlarmTime() {
        // given
        val time1 = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0)
        val time2 = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0)
        val time3 = LocalDateTime.now().plusDays(3).withHour(8).withMinute(0)

        val schedule1 = ScheduleFixture.builder()
            .appointmentAt(time1)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedule2 = ScheduleFixture.builder()
            .appointmentAt(time2)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedule3 = ScheduleFixture.builder()
            .appointmentAt(time3)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(schedule1, schedule2, schedule3)

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        // 2순위: 다음 알람 시간 오름차순 (가장 빠른 알람부터)
        assertThat(result).hasSize(3)
        assertThat(result[0].scheduleId).isEqualTo(schedule3.id) // 가장 빠른 알람
        assertThat(result[1].scheduleId).isEqualTo(schedule2.id)
        assertThat(result[2].scheduleId).isEqualTo(schedule1.id) // 가장 늦은 알람
    }

    @Test
    @DisplayName("반복 스케줄의 다음 알람 시간을 기준으로 정렬한다")
    fun toListOrderedByAlarmPriority_RepeatSchedule_SortsByNextOccurrence() {
        // given
        val now = LocalDateTime.now()
        val today = (now.dayOfWeek.value % 7) + 1 // 일(1) ~ 토(7)
        val tomorrow = (today % 7) + 1
        val dayAfterTomorrow = (tomorrow % 7) + 1

        val tomorrowDays = TreeSet<Int>()
        tomorrowDays.add(tomorrow)

        val dayAfterTomorrowDays = TreeSet<Int>()
        dayAfterTomorrowDays.add(dayAfterTomorrow)

        // 반복 스케줄 - 내일 09:00
        val repeatScheduleTomorrow = Schedule.createSchedule(
            1L,
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace(),
            AlarmFixture.enabledAlarm(now.withHour(8).withMinute(0)),
            AlarmFixture.enabledAlarm(now.withHour(8).withMinute(30)),
            "내일 반복 일정",
            true,
            tomorrowDays,
            now.withHour(9).withMinute(0),
            false,
            "메모"
        )

        // 반복 스케줄 - 모레 09:00
        val repeatScheduleDayAfter = Schedule.createSchedule(
            1L,
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace(),
            AlarmFixture.enabledAlarm(now.withHour(8).withMinute(0)),
            AlarmFixture.enabledAlarm(now.withHour(8).withMinute(30)),
            "모레 반복 일정",
            true,
            dayAfterTomorrowDays,
            now.withHour(9).withMinute(0),
            false,
            "메모"
        )

        // 일회성 스케줄 - 일주일 후 10:00
        val oneTimeSchedule = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(7).withHour(10).withMinute(0))
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(oneTimeSchedule, repeatScheduleDayAfter, repeatScheduleTomorrow)

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        // 반복 스케줄의 다음 발생 시간 기준으로 정렬
        assertThat(result).hasSize(3)
        assertThat(result[0].scheduleTitle).isEqualTo("내일 반복 일정")      // 가장 빠름
        assertThat(result[1].scheduleTitle).isEqualTo("모레 반복 일정")
        assertThat(result[2].scheduleTitle).isEqualTo("테스트 일정")          // 일주일 후
    }

    @Test
    @DisplayName("복합 시나리오: 알람 상태, 반복 여부, 시간을 모두 고려하여 정렬한다")
    fun toListOrderedByAlarmPriority_ComplexScenario() {
        // given
        val now = LocalDateTime.now()
        val today = (now.dayOfWeek.value % 7) + 1
        val tomorrow = (today % 7) + 1

        val tomorrowDays = TreeSet<Int>()
        tomorrowDays.add(tomorrow)

        // 1. 알람 ON + 반복 + 내일 08:00
        val onRepeatTomorrow = Schedule.createSchedule(
            1L,
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace(),
            AlarmFixture.enabledAlarm(now.withHour(7).withMinute(0)),
            AlarmFixture.enabledAlarm(now.withHour(7).withMinute(30)),
            "ON-반복-내일",
            true,
            tomorrowDays,
            now.withHour(8).withMinute(0),
            false,
            "메모"
        )

        // 2. 알람 ON + 비반복 + 3일 후 10:00
        val onOneTime3Days = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(3).withHour(10).withMinute(0))
            .onlyPreparationAlarmEnabled()
            .build()
        onOneTime3Days.setupQuickSchedule(1L, now.plusDays(3).withHour(10).withMinute(0))
        onOneTime3Days.registerPlaces(
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace()
        )

        // 3. 알람 ON + 비반복 + 5일 후 14:00
        val onOneTime5Days = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(5).withHour(14).withMinute(0))
            .onlyDepartureAlarmEnabled()
            .build()

        // 4. 알람 OFF + 반복
        val offRepeat = Schedule.createSchedule(
            1L,
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace(),
            AlarmFixture.disabledAlarm(now.withHour(9).withMinute(0)),
            AlarmFixture.disabledAlarm(now.withHour(9).withMinute(30)),
            "OFF-반복",
            true,
            tomorrowDays,
            now.withHour(10).withMinute(0),
            false,
            "메모"
        )

        // 5. 알람 OFF + 비반복 + 7일 후
        val offOneTime = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(7).withHour(16).withMinute(0))
            .disabledAlarms()
            .build()

        val schedules = listOf(
            offOneTime,
            onOneTime5Days,
            offRepeat,
            onOneTime3Days,
            onRepeatTomorrow
        )

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        /**
         * 예상 정렬 순서:
         * 1순위: 알람 활성화 상태 (ON > OFF)
         * 2순위: 다음 알람 시간 (오름차순)
         *
         * [알람 ON 그룹]
         * 1. ON-반복-내일 (내일 07:00 알람)
         * 2. ON-비반복-3일후 (3일 후 09:00 알람, appointmentAt - 1시간)
         * 3. ON-비반복-5일후 (5일 후 13:30 알람, appointmentAt - 30분)
         *
         * [알람 OFF 그룹]
         * 4. OFF-반복 (nextAlarmAt = null)
         * 5. OFF-비반복 (nextAlarmAt = null)
         */
        assertThat(result).hasSize(5)

        // 알람 ON 그룹
        assertThat(result[0].hasActiveAlarm).isTrue()
        assertThat(result[0].scheduleTitle).isEqualTo("ON-반복-내일")

        assertThat(result[1].hasActiveAlarm).isTrue()
        assertThat(result[1].scheduleId).isEqualTo(onOneTime3Days.id)

        assertThat(result[2].hasActiveAlarm).isTrue()
        assertThat(result[2].scheduleId).isEqualTo(onOneTime5Days.id)

        // 알람 OFF 그룹 (nextAlarmAt = null이므로 순서 보장 안 됨, 둘 다 마지막)
        assertThat(result[3].hasActiveAlarm).isFalse()
        assertThat(result[4].hasActiveAlarm).isFalse()
    }

    @Test
    @DisplayName("모든 알람이 비활성화된 스케줄들은 마지막에 위치한다")
    fun toListOrderedByAlarmPriority_DisabledAlarms_LastPosition() {
        // given
        val futureTime1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0)
        val futureTime2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)
        val futureTime3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0)

        val enabledSchedule = ScheduleFixture.builder()
            .appointmentAt(futureTime3)
            .onlyPreparationAlarmEnabled()
            .build()

        val disabledSchedule1 = ScheduleFixture.builder()
            .appointmentAt(futureTime1)
            .disabledAlarms()
            .build()

        val disabledSchedule2 = ScheduleFixture.builder()
            .appointmentAt(futureTime2)
            .disabledAlarms()
            .build()

        val schedules = listOf(disabledSchedule1, enabledSchedule, disabledSchedule2)

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        assertThat(result).hasSize(3)
        // 활성화된 알람이 첫 번째
        assertThat(result[0].hasActiveAlarm).isTrue()
        assertThat(result[0].scheduleId).isEqualTo(enabledSchedule.id)

        // 비활성화된 알람들은 마지막
        assertThat(result[1].hasActiveAlarm).isFalse()
        assertThat(result[2].hasActiveAlarm).isFalse()
    }

    @Test
    @DisplayName("준비 알람과 출발 알람 중 더 빠른 알람을 기준으로 정렬한다")
    fun toListOrderedByAlarmPriority_UsesEarlierAlarm() {
        // given
        val appointmentTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)

        // 준비 알람만 활성화 (1시간 전)
        val preparationOnly = ScheduleFixture.builder()
            .appointmentAt(appointmentTime)
            .onlyPreparationAlarmEnabled()
            .build()

        // 출발 알람만 활성화 (30분 전)
        val departureOnly = ScheduleFixture.builder()
            .appointmentAt(appointmentTime)
            .onlyDepartureAlarmEnabled()
            .build()

        val schedules = listOf(departureOnly, preparationOnly)

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        // 준비 알람(1시간 전)이 출발 알람(30분 전)보다 빠르므로 먼저 옴
        assertThat(result).hasSize(2)
        assertThat(result[0].scheduleId).isEqualTo(preparationOnly.id)
        assertThat(result[1].scheduleId).isEqualTo(departureOnly.id)
    }

    @Test
    @DisplayName("빈 리스트는 빈 리스트를 반환한다")
    fun toListOrderedByAlarmPriority_EmptyList_ReturnsEmpty() {
        // given
        val schedules = listOf<Schedule>()

        // when
        val result = mapper.toListOrderedByAlarmPriority(schedules)

        // then
        assertThat(result).isEmpty()
    }

    // ========================================
    // earliestAlarmId 도출 로직 검증 테스트
    // ========================================

    /**
     * ScheduleQueryFacade.findEarliestActiveAlarmScheduleId와 동일한 로직
     * 정렬된 목록에서 첫 번째 활성화된 알람이 있는 스케줄의 ID를 반환
     */
    private fun findEarliestActiveAlarmScheduleId(sortedItems: List<HomeScheduleListItem>): Long? =
        sortedItems
            .filter { it.hasActiveAlarm }
            .firstOrNull()
            ?.scheduleId

    @Test
    @DisplayName("earliestAlarmId: 여러 활성화된 스케줄 중 가장 빠른 알람의 스케줄 ID를 반환한다")
    fun findEarliestAlarmId_MultipleActiveSchedules_ReturnsEarliestScheduleId() {
        // given
        val time1 = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0)
        val time2 = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0)  // 가장 빠름
        val time3 = LocalDateTime.now().plusDays(5).withHour(8).withMinute(0)

        val schedule1 = ScheduleFixture.builder()
            .appointmentAt(time1)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedule2 = ScheduleFixture.builder()
            .appointmentAt(time2)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedule3 = ScheduleFixture.builder()
            .appointmentAt(time3)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(schedule1, schedule2, schedule3)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isEqualTo(schedule2.id)
        assertThat(sortedItems[0].scheduleId).isEqualTo(schedule2.id)
    }

    @Test
    @DisplayName("earliestAlarmId: 활성화/비활성화 혼합 시 활성화된 것 중 가장 빠른 스케줄 ID를 반환한다")
    fun findEarliestAlarmId_MixedActiveAndDisabled_ReturnsEarliestActiveScheduleId() {
        // given
        val time1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0)   // 비활성화 - 가장 빠름
        val time2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)  // 활성화 - 두번째 빠름
        val time3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0)  // 활성화 - 세번째 빠름

        val disabledSchedule = ScheduleFixture.builder()
            .appointmentAt(time1)
            .disabledAlarms()
            .build()

        val enabledSchedule1 = ScheduleFixture.builder()
            .appointmentAt(time2)
            .onlyPreparationAlarmEnabled()
            .build()

        val enabledSchedule2 = ScheduleFixture.builder()
            .appointmentAt(time3)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(disabledSchedule, enabledSchedule2, enabledSchedule1)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isEqualTo(enabledSchedule1.id)
        assertThat(sortedItems[0].scheduleId).isEqualTo(enabledSchedule1.id)
        assertThat(sortedItems[0].hasActiveAlarm).isTrue()
    }

    @Test
    @DisplayName("earliestAlarmId: 빈 리스트인 경우 null을 반환한다")
    fun findEarliestAlarmId_EmptyList_ReturnsNull() {
        // given
        val schedules = listOf<Schedule>()

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isNull()
        assertThat(sortedItems).isEmpty()
    }

    @Test
    @DisplayName("earliestAlarmId: 모든 스케줄이 비활성화된 경우 null을 반환한다")
    fun findEarliestAlarmId_AllDisabled_ReturnsNull() {
        // given
        val time1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0)
        val time2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)
        val time3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0)

        val disabled1 = ScheduleFixture.builder()
            .appointmentAt(time1)
            .disabledAlarms()
            .build()

        val disabled2 = ScheduleFixture.builder()
            .appointmentAt(time2)
            .disabledAlarms()
            .build()

        val disabled3 = ScheduleFixture.builder()
            .appointmentAt(time3)
            .disabledAlarms()
            .build()

        val schedules = listOf(disabled1, disabled2, disabled3)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isNull()
        assertThat(sortedItems).hasSize(3)
        assertThat(sortedItems).allMatch { !it.hasActiveAlarm }
    }

    @Test
    @DisplayName("earliestAlarmId: 단일 활성화 스케줄인 경우 해당 스케줄 ID를 반환한다")
    fun findEarliestAlarmId_SingleActiveSchedule_ReturnsScheduleId() {
        // given
        val futureTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)

        val schedule = ScheduleFixture.builder()
            .appointmentAt(futureTime)
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(schedule)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isEqualTo(schedule.id)
        assertThat(sortedItems).hasSize(1)
        assertThat(sortedItems[0].hasActiveAlarm).isTrue()
    }

    @Test
    @DisplayName("earliestAlarmId: 단일 비활성화 스케줄인 경우 null을 반환한다")
    fun findEarliestAlarmId_SingleDisabledSchedule_ReturnsNull() {
        // given
        val futureTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0)

        val schedule = ScheduleFixture.builder()
            .appointmentAt(futureTime)
            .disabledAlarms()
            .build()

        val schedules = listOf(schedule)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isNull()
        assertThat(sortedItems).hasSize(1)
        assertThat(sortedItems[0].hasActiveAlarm).isFalse()
    }

    @Test
    @DisplayName("earliestAlarmId: 복합 시나리오 - ON/OFF 혼합, 반복/비반복 혼합")
    fun findEarliestAlarmId_ComplexScenario() {
        // given
        val now = LocalDateTime.now()
        val today = (now.dayOfWeek.value % 7) + 1
        val tomorrow = (today % 7) + 1

        val tomorrowDays = TreeSet<Int>()
        tomorrowDays.add(tomorrow)

        // 1. 알람 ON + 반복 + 내일 08:00 (가장 빠른 활성화 알람)
        val onRepeatTomorrow = Schedule.createSchedule(
            1L,
            PlaceFixture.defaultDeparturePlace(),
            PlaceFixture.defaultArrivalPlace(),
            AlarmFixture.enabledAlarm(now.withHour(7).withMinute(0)),
            AlarmFixture.enabledAlarm(now.withHour(7).withMinute(30)),
            "ON-반복-내일",
            true,
            tomorrowDays,
            now.withHour(8).withMinute(0),
            false,
            "메모"
        )

        // 2. 알람 OFF + 비반복 + 2일 후 (시간상 가장 빠르지만 비활성화)
        val offOneTime = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(2).withHour(6).withMinute(0))
            .disabledAlarms()
            .build()

        // 3. 알람 ON + 비반복 + 5일 후
        val onOneTime = ScheduleFixture.builder()
            .appointmentAt(now.plusDays(5).withHour(14).withMinute(0))
            .onlyPreparationAlarmEnabled()
            .build()

        val schedules = listOf(offOneTime, onOneTime, onRepeatTomorrow)

        // when
        val sortedItems = mapper.toListOrderedByAlarmPriority(schedules)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems)

        // then
        assertThat(earliestAlarmId).isEqualTo(onRepeatTomorrow.id)
        assertThat(sortedItems[0].scheduleTitle).isEqualTo("ON-반복-내일")
        assertThat(sortedItems[0].hasActiveAlarm).isTrue()
    }
}
