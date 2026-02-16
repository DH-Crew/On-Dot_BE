package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.fixture.AlarmFixture;
import com.dh.ondot.schedule.fixture.PlaceFixture;
import com.dh.ondot.schedule.fixture.ScheduleFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HomeScheduleListItemMapper 테스트")
class HomeScheduleListItemMapperTest {

    private final HomeScheduleListItemMapper mapper = new HomeScheduleListItemMapper();

    @Test
    @DisplayName("알람 활성화 상태에 따라 정렬한다 - ON 알람이 OFF 알람보다 우선")
    void toListOrderedByAlarmPriority_SortsByAlarmState() {
        // given
        LocalDateTime futureTime1 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
        LocalDateTime futureTime2 = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0);

        // 알람 OFF - 더 빠른 시간이지만 비활성화
        Schedule disabledSchedule = ScheduleFixture.builder()
                .appointmentAt(futureTime2)
                .disabledAlarms()
                .build();

        // 알람 ON - 더 늦은 시간이지만 활성화
        Schedule enabledSchedule = ScheduleFixture.builder()
                .appointmentAt(futureTime1)
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(disabledSchedule, enabledSchedule);

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        // 1순위: 알람 활성화 상태 (ON > OFF)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHasActiveAlarm()).isTrue();  // enabledSchedule
        assertThat(result.get(1).getHasActiveAlarm()).isFalse(); // disabledSchedule
    }

    @Test
    @DisplayName("동일한 활성화 상태에서는 다음 알람 시간 순으로 정렬한다")
    void toListOrderedByAlarmPriority_SortsByNextAlarmTime() {
        // given
        LocalDateTime time1 = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0);
        LocalDateTime time3 = LocalDateTime.now().plusDays(3).withHour(8).withMinute(0);

        Schedule schedule1 = ScheduleFixture.builder()
                .appointmentAt(time1)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule schedule2 = ScheduleFixture.builder()
                .appointmentAt(time2)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule schedule3 = ScheduleFixture.builder()
                .appointmentAt(time3)
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(schedule1, schedule2, schedule3);

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        // 2순위: 다음 알람 시간 오름차순 (가장 빠른 알람부터)
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getScheduleId()).isEqualTo(schedule3.getId()); // 가장 빠른 알람
        assertThat(result.get(1).getScheduleId()).isEqualTo(schedule2.getId());
        assertThat(result.get(2).getScheduleId()).isEqualTo(schedule1.getId()); // 가장 늦은 알람
    }

    @Test
    @DisplayName("반복 스케줄의 다음 알람 시간을 기준으로 정렬한다")
    void toListOrderedByAlarmPriority_RepeatSchedule_SortsByNextOccurrence() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int today = (now.getDayOfWeek().getValue() % 7) + 1; // 일(1) ~ 토(7)
        int tomorrow = (today % 7) + 1;
        int dayAfterTomorrow = (tomorrow % 7) + 1;

        SortedSet<Integer> tomorrowDays = new TreeSet<>();
        tomorrowDays.add(tomorrow);

        SortedSet<Integer> dayAfterTomorrowDays = new TreeSet<>();
        dayAfterTomorrowDays.add(dayAfterTomorrow);

        // 반복 스케줄 - 내일 09:00
        Schedule repeatScheduleTomorrow = Schedule.createSchedule(
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
        );

        // 반복 스케줄 - 모레 09:00
        Schedule repeatScheduleDayAfter = Schedule.createSchedule(
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
        );

        // 일회성 스케줄 - 일주일 후 10:00
        Schedule oneTimeSchedule = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(7).withHour(10).withMinute(0))
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(oneTimeSchedule, repeatScheduleDayAfter, repeatScheduleTomorrow);

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        // 반복 스케줄의 다음 발생 시간 기준으로 정렬
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getScheduleTitle()).isEqualTo("내일 반복 일정");      // 가장 빠름
        assertThat(result.get(1).getScheduleTitle()).isEqualTo("모레 반복 일정");
        assertThat(result.get(2).getScheduleTitle()).isEqualTo("테스트 일정");          // 일주일 후
    }

    @Test
    @DisplayName("복합 시나리오: 알람 상태, 반복 여부, 시간을 모두 고려하여 정렬한다")
    void toListOrderedByAlarmPriority_ComplexScenario() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int today = (now.getDayOfWeek().getValue() % 7) + 1;
        int tomorrow = (today % 7) + 1;

        SortedSet<Integer> tomorrowDays = new TreeSet<>();
        tomorrowDays.add(tomorrow);

        // 1. 알람 ON + 반복 + 내일 08:00
        Schedule onRepeatTomorrow = Schedule.createSchedule(
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
        );

        // 2. 알람 ON + 비반복 + 3일 후 10:00
        Schedule onOneTime3Days = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(3).withHour(10).withMinute(0))
                .onlyPreparationAlarmEnabled()
                .build();
        onOneTime3Days.setupQuickSchedule(1L, now.plusDays(3).withHour(10).withMinute(0));
        onOneTime3Days.registerPlaces(
                PlaceFixture.defaultDeparturePlace(),
                PlaceFixture.defaultArrivalPlace()
        );

        // 3. 알람 ON + 비반복 + 5일 후 14:00
        Schedule onOneTime5Days = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(5).withHour(14).withMinute(0))
                .onlyDepartureAlarmEnabled()
                .build();

        // 4. 알람 OFF + 반복
        Schedule offRepeat = Schedule.createSchedule(
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
        );

        // 5. 알람 OFF + 비반복 + 7일 후
        Schedule offOneTime = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(7).withHour(16).withMinute(0))
                .disabledAlarms()
                .build();

        List<Schedule> schedules = List.of(
                offOneTime,
                onOneTime5Days,
                offRepeat,
                onOneTime3Days,
                onRepeatTomorrow
        );

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

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
        assertThat(result).hasSize(5);

        // 알람 ON 그룹
        assertThat(result.get(0).getHasActiveAlarm()).isTrue();
        assertThat(result.get(0).getScheduleTitle()).isEqualTo("ON-반복-내일");

        assertThat(result.get(1).getHasActiveAlarm()).isTrue();
        assertThat(result.get(1).getScheduleId()).isEqualTo(onOneTime3Days.getId());

        assertThat(result.get(2).getHasActiveAlarm()).isTrue();
        assertThat(result.get(2).getScheduleId()).isEqualTo(onOneTime5Days.getId());

        // 알람 OFF 그룹 (nextAlarmAt = null이므로 순서 보장 안 됨, 둘 다 마지막)
        assertThat(result.get(3).getHasActiveAlarm()).isFalse();
        assertThat(result.get(4).getHasActiveAlarm()).isFalse();
    }

    @Test
    @DisplayName("모든 알람이 비활성화된 스케줄들은 마지막에 위치한다")
    void toListOrderedByAlarmPriority_DisabledAlarms_LastPosition() {
        // given
        LocalDateTime futureTime1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        LocalDateTime futureTime2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
        LocalDateTime futureTime3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0);

        Schedule enabledSchedule = ScheduleFixture.builder()
                .appointmentAt(futureTime3)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule disabledSchedule1 = ScheduleFixture.builder()
                .appointmentAt(futureTime1)
                .disabledAlarms()
                .build();

        Schedule disabledSchedule2 = ScheduleFixture.builder()
                .appointmentAt(futureTime2)
                .disabledAlarms()
                .build();

        List<Schedule> schedules = List.of(disabledSchedule1, enabledSchedule, disabledSchedule2);

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        assertThat(result).hasSize(3);
        // 활성화된 알람이 첫 번째
        assertThat(result.get(0).getHasActiveAlarm()).isTrue();
        assertThat(result.get(0).getScheduleId()).isEqualTo(enabledSchedule.getId());

        // 비활성화된 알람들은 마지막
        assertThat(result.get(1).getHasActiveAlarm()).isFalse();
        assertThat(result.get(2).getHasActiveAlarm()).isFalse();
    }

    @Test
    @DisplayName("준비 알람과 출발 알람 중 더 빠른 알람을 기준으로 정렬한다")
    void toListOrderedByAlarmPriority_UsesEarlierAlarm() {
        // given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);

        // 준비 알람만 활성화 (1시간 전)
        Schedule preparationOnly = ScheduleFixture.builder()
                .appointmentAt(appointmentTime)
                .onlyPreparationAlarmEnabled()
                .build();

        // 출발 알람만 활성화 (30분 전)
        Schedule departureOnly = ScheduleFixture.builder()
                .appointmentAt(appointmentTime)
                .onlyDepartureAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(departureOnly, preparationOnly);

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        // 준비 알람(1시간 전)이 출발 알람(30분 전)보다 빠르므로 먼저 옴
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScheduleId()).isEqualTo(preparationOnly.getId());
        assertThat(result.get(1).getScheduleId()).isEqualTo(departureOnly.getId());
    }

    @Test
    @DisplayName("빈 리스트는 빈 리스트를 반환한다")
    void toListOrderedByAlarmPriority_EmptyList_ReturnsEmpty() {
        // given
        List<Schedule> schedules = List.of();

        // when
        List<HomeScheduleListItem> result = mapper.toListOrderedByAlarmPriority(schedules);

        // then
        assertThat(result).isEmpty();
    }

    // ========================================
    // earliestAlarmId 도출 로직 검증 테스트
    // ========================================

    /**
     * ScheduleQueryFacade.findEarliestActiveAlarmScheduleId와 동일한 로직
     * 정렬된 목록에서 첫 번째 활성화된 알람이 있는 스케줄의 ID를 반환
     */
    private Long findEarliestActiveAlarmScheduleId(List<HomeScheduleListItem> sortedItems) {
        return sortedItems.stream()
                .filter(HomeScheduleListItem::getHasActiveAlarm)
                .findFirst()
                .map(HomeScheduleListItem::getScheduleId)
                .orElse(null);
    }

    @Test
    @DisplayName("earliestAlarmId: 여러 활성화된 스케줄 중 가장 빠른 알람의 스케줄 ID를 반환한다")
    void findEarliestAlarmId_MultipleActiveSchedules_ReturnsEarliestScheduleId() {
        // given
        LocalDateTime time1 = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0);  // 가장 빠름
        LocalDateTime time3 = LocalDateTime.now().plusDays(5).withHour(8).withMinute(0);

        Schedule schedule1 = ScheduleFixture.builder()
                .appointmentAt(time1)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule schedule2 = ScheduleFixture.builder()
                .appointmentAt(time2)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule schedule3 = ScheduleFixture.builder()
                .appointmentAt(time3)
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(schedule1, schedule2, schedule3);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        /**
         * 예상 결과:
         * - 정렬 순서: schedule2(3일 후) → schedule3(5일 후) → schedule1(7일 후)
         * - earliestAlarmId = schedule2의 ID
         */
        assertThat(earliestAlarmId).isEqualTo(schedule2.getId());
        assertThat(sortedItems.get(0).getScheduleId()).isEqualTo(schedule2.getId());
    }

    @Test
    @DisplayName("earliestAlarmId: 활성화/비활성화 혼합 시 활성화된 것 중 가장 빠른 스케줄 ID를 반환한다")
    void findEarliestAlarmId_MixedActiveAndDisabled_ReturnsEarliestActiveScheduleId() {
        // given
        LocalDateTime time1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);   // 비활성화 - 가장 빠름
        LocalDateTime time2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);  // 활성화 - 두번째 빠름
        LocalDateTime time3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0);  // 활성화 - 세번째 빠름

        Schedule disabledSchedule = ScheduleFixture.builder()
                .appointmentAt(time1)
                .disabledAlarms()
                .build();

        Schedule enabledSchedule1 = ScheduleFixture.builder()
                .appointmentAt(time2)
                .onlyPreparationAlarmEnabled()
                .build();

        Schedule enabledSchedule2 = ScheduleFixture.builder()
                .appointmentAt(time3)
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(disabledSchedule, enabledSchedule2, enabledSchedule1);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        /**
         * 예상 결과:
         * - 정렬 순서: enabledSchedule1(5일후) → enabledSchedule2(7일후) → disabledSchedule(2일후)
         * - 비활성화된 스케줄은 무시하고, 활성화된 것 중 가장 빠른 스케줄 ID 반환
         * - earliestAlarmId = enabledSchedule1의 ID
         */
        assertThat(earliestAlarmId).isEqualTo(enabledSchedule1.getId());
        assertThat(sortedItems.get(0).getScheduleId()).isEqualTo(enabledSchedule1.getId());
        assertThat(sortedItems.get(0).getHasActiveAlarm()).isTrue();
    }

    @Test
    @DisplayName("earliestAlarmId: 빈 리스트인 경우 null을 반환한다")
    void findEarliestAlarmId_EmptyList_ReturnsNull() {
        // given
        List<Schedule> schedules = List.of();

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        assertThat(earliestAlarmId).isNull();
        assertThat(sortedItems).isEmpty();
    }

    @Test
    @DisplayName("earliestAlarmId: 모든 스케줄이 비활성화된 경우 null을 반환한다")
    void findEarliestAlarmId_AllDisabled_ReturnsNull() {
        // given
        LocalDateTime time1 = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
        LocalDateTime time3 = LocalDateTime.now().plusDays(7).withHour(11).withMinute(0);

        Schedule disabled1 = ScheduleFixture.builder()
                .appointmentAt(time1)
                .disabledAlarms()
                .build();

        Schedule disabled2 = ScheduleFixture.builder()
                .appointmentAt(time2)
                .disabledAlarms()
                .build();

        Schedule disabled3 = ScheduleFixture.builder()
                .appointmentAt(time3)
                .disabledAlarms()
                .build();

        List<Schedule> schedules = List.of(disabled1, disabled2, disabled3);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        /**
         * 예상 결과:
         * - 활성화된 알람이 없으므로 null 반환
         * - 모든 스케줄이 hasActiveAlarm = false
         */
        assertThat(earliestAlarmId).isNull();
        assertThat(sortedItems).hasSize(3);
        assertThat(sortedItems).allMatch(item -> !item.getHasActiveAlarm());
    }

    @Test
    @DisplayName("earliestAlarmId: 단일 활성화 스케줄인 경우 해당 스케줄 ID를 반환한다")
    void findEarliestAlarmId_SingleActiveSchedule_ReturnsScheduleId() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);

        Schedule schedule = ScheduleFixture.builder()
                .appointmentAt(futureTime)
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(schedule);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        assertThat(earliestAlarmId).isEqualTo(schedule.getId());
        assertThat(sortedItems).hasSize(1);
        assertThat(sortedItems.get(0).getHasActiveAlarm()).isTrue();
    }

    @Test
    @DisplayName("earliestAlarmId: 단일 비활성화 스케줄인 경우 null을 반환한다")
    void findEarliestAlarmId_SingleDisabledSchedule_ReturnsNull() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);

        Schedule schedule = ScheduleFixture.builder()
                .appointmentAt(futureTime)
                .disabledAlarms()
                .build();

        List<Schedule> schedules = List.of(schedule);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        assertThat(earliestAlarmId).isNull();
        assertThat(sortedItems).hasSize(1);
        assertThat(sortedItems.get(0).getHasActiveAlarm()).isFalse();
    }

    @Test
    @DisplayName("earliestAlarmId: 복합 시나리오 - ON/OFF 혼합, 반복/비반복 혼합")
    void findEarliestAlarmId_ComplexScenario() {
        // given
        LocalDateTime now = LocalDateTime.now();
        int today = (now.getDayOfWeek().getValue() % 7) + 1;
        int tomorrow = (today % 7) + 1;

        SortedSet<Integer> tomorrowDays = new TreeSet<>();
        tomorrowDays.add(tomorrow);

        // 1. 알람 ON + 반복 + 내일 08:00 (가장 빠른 활성화 알람)
        Schedule onRepeatTomorrow = Schedule.createSchedule(
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
        );

        // 2. 알람 OFF + 비반복 + 2일 후 (시간상 가장 빠르지만 비활성화)
        Schedule offOneTime = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(2).withHour(6).withMinute(0))
                .disabledAlarms()
                .build();

        // 3. 알람 ON + 비반복 + 5일 후
        Schedule onOneTime = ScheduleFixture.builder()
                .appointmentAt(now.plusDays(5).withHour(14).withMinute(0))
                .onlyPreparationAlarmEnabled()
                .build();

        List<Schedule> schedules = List.of(offOneTime, onOneTime, onRepeatTomorrow);

        // when
        List<HomeScheduleListItem> sortedItems = mapper.toListOrderedByAlarmPriority(schedules);
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(sortedItems);

        // then
        /**
         * 예상 결과:
         * - 정렬 순서: onRepeatTomorrow → onOneTime → offOneTime
         * - earliestAlarmId = onRepeatTomorrow의 ID (반복 스케줄의 다음 알람이 가장 빠름)
         */
        assertThat(earliestAlarmId).isEqualTo(onRepeatTomorrow.getId());
        assertThat(sortedItems.get(0).getScheduleTitle()).isEqualTo("ON-반복-내일");
        assertThat(sortedItems.get(0).getHasActiveAlarm()).isTrue();
    }
}
