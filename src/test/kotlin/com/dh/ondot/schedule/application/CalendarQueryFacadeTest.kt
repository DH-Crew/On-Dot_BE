package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.core.exception.CalendarDateRangeTooLargeException
import com.dh.ondot.schedule.core.exception.InvalidCalendarDateRangeException
import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.fixture.MemberFixture
import com.dh.ondot.schedule.fixture.MockitoHelper.anyNonNull
import com.dh.ondot.schedule.fixture.MockitoHelper.eqNonNull
import com.dh.ondot.schedule.fixture.ScheduleFixture
import com.dh.ondot.schedule.infra.CalendarQueryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarQueryFacade 테스트")
class CalendarQueryFacadeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var calendarQueryRepository: CalendarQueryRepository
    @Mock private lateinit var exclusionService: CalendarRecordExclusionService

    @InjectMocks private lateinit var facade: CalendarQueryFacade

    private val memberId = 1L

    private fun stubMember() {
        given(memberService.getMemberIfExists(memberId)).willReturn(MemberFixture.defaultMember())
    }

    private fun stubSchedules(startDate: LocalDate, endDate: LocalDate, schedules: List<com.dh.ondot.schedule.domain.Schedule>) {
        given(calendarQueryRepository.findSchedulesForCalendarRange(
            eqNonNull(memberId), anyNonNull(), anyNonNull(),
        )).willReturn(schedules)
        given(exclusionService.findExclusionsInRange(memberId, startDate, endDate)).willReturn(emptyList())
    }

    private fun stubSchedulesWithExclusions(
        startDate: LocalDate, endDate: LocalDate,
        schedules: List<com.dh.ondot.schedule.domain.Schedule>,
        exclusions: List<CalendarRecordExclusion>,
    ) {
        given(calendarQueryRepository.findSchedulesForCalendarRange(
            eqNonNull(memberId), anyNonNull(), anyNonNull(),
        )).willReturn(schedules)
        given(exclusionService.findExclusionsInRange(memberId, startDate, endDate)).willReturn(exclusions)
    }

    @Nested
    @DisplayName("날짜 범위 검증")
    inner class DateRangeValidation {
        @Test
        @DisplayName("startDate > endDate이면 예외 발생")
        fun invalidRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(memberId, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 1))
            }.isInstanceOf(InvalidCalendarDateRangeException::class.java)
        }

        @Test
        @DisplayName("45일 초과 범위이면 예외 발생")
        fun tooLargeRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(memberId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1))
            }.isInstanceOf(CalendarDateRangeTooLargeException::class.java)
        }
    }

    @Nested
    @DisplayName("범위 조회")
    inner class RangeQuery {
        @Test
        @DisplayName("스케줄이 없으면 빈 리스트를 반환한다")
        fun noSchedules_ReturnsEmpty() {
            val startDate = LocalDate.of(2026, 3, 1)
            val endDate = LocalDate.of(2026, 3, 31)
            stubMember()
            stubSchedules(startDate, endDate, emptyList())

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("과거 비반복 스케줄은 RECORD 타입으로 표시된다")
        fun pastNonRepeatSchedule_IsRecord() {
            // 2026-01-10 14:00 약속 (과거)
            val appointmentDate = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .title("과거 일정")
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(appointmentDate, appointmentDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).hasSize(1)
            assertThat(result[0].date).isEqualTo(appointmentDate)
            assertThat(result[0].schedules).hasSize(1)
            assertThat(result[0].schedules[0].type).isEqualTo(CalendarScheduleType.RECORD)
            assertThat(result[0].schedules[0].title).isEqualTo("과거 일정")
        }

        @Test
        @DisplayName("미래 비반복 스케줄은 ALARM 타입으로 표시된다")
        fun futureNonRepeatSchedule_IsAlarm() {
            val appointmentDate = LocalDate.of(2099, 12, 31)
            val schedule = ScheduleFixture.builder()
                .title("미래 일정")
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2099, 12, 1), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(appointmentDate, appointmentDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).hasSize(1)
            assertThat(result[0].schedules[0].type).isEqualTo(CalendarScheduleType.ALARM)
        }

        @Test
        @DisplayName("반복 스케줄이 범위 내 해당 요일에 확장된다")
        fun repeatSchedule_ExpandsToMatchingDays() {
            // 매일 반복, 2026-03-01에 생성, 14:00 약속
            val schedule = ScheduleFixture.builder()
                .title("매일 반복")
                .isRepeat(true)
                .repeatDays(ScheduleFixture.allDays())
                .appointmentAt(LocalDate.of(2026, 3, 1).atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 3, 1), LocalTime.of(10, 0)))
                .build()

            // 2026-03-01 ~ 2026-03-03 (3일간)
            val startDate = LocalDate.of(2026, 3, 1)
            val endDate = LocalDate.of(2026, 3, 3)
            stubMember()
            stubSchedules(startDate, endDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            assertThat(result).hasSize(3)
            assertThat(result.map { it.date }).containsExactly(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 3),
            )
            result.forEach { day ->
                assertThat(day.schedules).hasSize(1)
                assertThat(day.schedules[0].isRepeat).isTrue()
            }
        }

        @Test
        @DisplayName("반복 스케줄은 createdAt 이전 날짜에 표시되지 않는다")
        fun repeatSchedule_NotShownBeforeCreatedDate() {
            // 2026-03-05에 생성된 매일 반복 스케줄
            val schedule = ScheduleFixture.builder()
                .isRepeat(true)
                .repeatDays(ScheduleFixture.allDays())
                .appointmentAt(LocalDate.of(2026, 3, 5).atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 3, 5), LocalTime.of(10, 0)))
                .build()

            val startDate = LocalDate.of(2026, 3, 1)
            val endDate = LocalDate.of(2026, 3, 7)
            stubMember()
            stubSchedules(startDate, endDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            assertThat(result.map { it.date }).containsExactly(
                LocalDate.of(2026, 3, 5),
                LocalDate.of(2026, 3, 6),
                LocalDate.of(2026, 3, 7),
            )
        }

        @Test
        @DisplayName("소프트 삭제된 비반복 스케줄은 삭제 전 약속이면 RECORD로 표시된다")
        fun deletedNonRepeatSchedule_ShownAsRecordBeforeDeletion() {
            // 약속 2026-01-10 14:00, 삭제 2026-01-15
            val appointmentDate = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .title("삭제된 일정")
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 15), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(appointmentDate, appointmentDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).hasSize(1)
            assertThat(result[0].schedules[0].type).isEqualTo(CalendarScheduleType.RECORD)
        }

        @Test
        @DisplayName("소프트 삭제된 비반복 스케줄은 삭제 시점 이후 약속이면 표시되지 않는다")
        fun deletedNonRepeatSchedule_NotShownIfDeletedBeforeAppointment() {
            // 약속 2026-01-20 14:00, 삭제 2026-01-15 (삭제가 약속 전)
            val appointmentDate = LocalDate.of(2026, 1, 20)
            val schedule = ScheduleFixture.builder()
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 15), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(appointmentDate, appointmentDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("소프트 삭제된 반복 스케줄은 삭제 시점 이전 날짜만 RECORD로 표시된다")
        fun deletedRepeatSchedule_OnlyShownBeforeDeletion() {
            // 매일 반복, 2026-01-01 생성, 2026-01-05 삭제
            val schedule = ScheduleFixture.builder()
                .isRepeat(true)
                .repeatDays(ScheduleFixture.allDays())
                .appointmentAt(LocalDate.of(2026, 1, 1).atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 5), LocalTime.of(10, 0)))
                .build()

            val startDate = LocalDate.of(2026, 1, 1)
            val endDate = LocalDate.of(2026, 1, 7)
            stubMember()
            stubSchedules(startDate, endDate, listOf(schedule))

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            // 1/1 ~ 1/4만 표시 (1/5 14:00은 deletedAt 10:00 이후이므로 제외)
            assertThat(result).hasSize(4)
            assertThat(result.map { it.date }).containsExactly(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 1, 3),
                LocalDate.of(2026, 1, 4),
            )
            result.forEach { day ->
                assertThat(day.schedules[0].type).isEqualTo(CalendarScheduleType.RECORD)
            }
        }

        @Test
        @DisplayName("exclusion에 해당하는 과거 기록은 제외된다")
        fun excludedRecord_IsFiltered() {
            // 2026-01-10 14:00 약속 (과거)
            val appointmentDate = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .build()

            val exclusion = CalendarRecordExclusion.create(memberId, schedule.id, appointmentDate)
            stubMember()
            stubSchedulesWithExclusions(
                appointmentDate, appointmentDate, listOf(schedule), listOf(exclusion),
            )

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("exclusion은 ALARM 타입 스케줄에 영향을 주지 않는다")
        fun exclusion_DoesNotAffectAlarm() {
            val appointmentDate = LocalDate.of(2099, 12, 31)
            val schedule = ScheduleFixture.builder()
                .appointmentAt(appointmentDate.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2099, 12, 1), LocalTime.of(10, 0)))
                .build()

            val exclusion = CalendarRecordExclusion.create(memberId, schedule.id, appointmentDate)
            stubMember()
            stubSchedulesWithExclusions(
                appointmentDate, appointmentDate, listOf(schedule), listOf(exclusion),
            )

            val result = facade.getCalendarRange(memberId, appointmentDate, appointmentDate)

            assertThat(result).hasSize(1)
            assertThat(result[0].schedules[0].type).isEqualTo(CalendarScheduleType.ALARM)
        }
    }

    @Nested
    @DisplayName("여러 스케줄 복합 조회")
    inner class MultipleSchedules {
        @Test
        @DisplayName("같은 날짜에 비반복 2개 + 반복 1개가 시간순으로 함께 표시된다")
        fun multipleSchedules_SameDateGroupedAndSorted() {
            val date = LocalDate.of(2026, 1, 10)
            val nonRepeat1 = ScheduleFixture.builder()
                .title("병원 예약")
                .appointmentAt(date.atTime(10, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()
            val nonRepeat2 = ScheduleFixture.builder()
                .title("점심 약속")
                .appointmentAt(date.atTime(12, 30))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 5), LocalTime.of(9, 0)))
                .build()
            val repeat = ScheduleFixture.builder()
                .title("출근")
                .isRepeat(true)
                .repeatDays(ScheduleFixture.allDays())
                .appointmentAt(date.atTime(8, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()

            stubMember()
            stubSchedules(date, date, listOf(nonRepeat1, nonRepeat2, repeat))

            val result = facade.getCalendarRange(memberId, date, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].date).isEqualTo(date)
            assertThat(result[0].schedules).hasSize(3)
            assertThat(result[0].schedules.map { it.title }).containsExactly("출근", "병원 예약", "점심 약속")
        }

        @Test
        @DisplayName("여러 날짜에 걸친 스케줄이 날짜별로 그룹핑되고 빈 날짜는 생략된다")
        fun multipleSchedules_AcrossDates_EmptyDaysOmitted() {
            val schedule1 = ScheduleFixture.builder()
                .title("1/10 일정")
                .appointmentAt(LocalDate.of(2026, 1, 10).atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()
            val schedule2 = ScheduleFixture.builder()
                .title("1/12 일정")
                .appointmentAt(LocalDate.of(2026, 1, 12).atTime(10, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()

            val startDate = LocalDate.of(2026, 1, 10)
            val endDate = LocalDate.of(2026, 1, 15)
            stubMember()
            stubSchedules(startDate, endDate, listOf(schedule1, schedule2))

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            assertThat(result).hasSize(2)
            assertThat(result.map { it.date }).containsExactly(
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 12),
            )
        }

        @Test
        @DisplayName("삭제된 스케줄과 활성 스케줄이 혼합된 경우 정확히 필터링된다")
        fun deletedAndActiveSchedules_FilteredCorrectly() {
            val date = LocalDate.of(2026, 1, 10)
            val active = ScheduleFixture.builder()
                .title("활성 일정")
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()
            // 약속 후 삭제 → 기록 남아야 함
            val deletedAfter = ScheduleFixture.builder()
                .title("삭제됐지만 기록 남음")
                .appointmentAt(date.atTime(16, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 15), LocalTime.of(10, 0)))
                .build()
            // 약속 전 삭제 → 표시 안 됨
            val deletedBefore = ScheduleFixture.builder()
                .title("삭제됨 미표시")
                .appointmentAt(date.atTime(18, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 9), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(date, date, listOf(active, deletedAfter, deletedBefore))

            val result = facade.getCalendarRange(memberId, date, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].schedules).hasSize(2)
            assertThat(result[0].schedules.map { it.title }).containsExactly("활성 일정", "삭제됐지만 기록 남음")
        }

        @Test
        @DisplayName("평일 반복 스케줄과 비반복 스케줄이 한 주간 범위에서 함께 표시된다")
        fun weekdayRepeatAndNonRepeat_OneWeekRange() {
            val nonRepeat = ScheduleFixture.builder()
                .title("1/6 단건")
                .appointmentAt(LocalDate.of(2026, 1, 6).atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()
            val repeat = ScheduleFixture.builder()
                .title("출근")
                .isRepeat(true)
                .repeatDays(ScheduleFixture.weekdays())
                .appointmentAt(LocalDate.of(2026, 1, 1).atTime(9, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(8, 0)))
                .build()

            // 2026-01-05(월) ~ 2026-01-11(일)
            val startDate = LocalDate.of(2026, 1, 5)
            val endDate = LocalDate.of(2026, 1, 11)
            stubMember()
            stubSchedules(startDate, endDate, listOf(nonRepeat, repeat))

            val result = facade.getCalendarRange(memberId, startDate, endDate)

            // 월~금 5일만 (토/일은 평일 반복에 해당 안 됨, 비반복도 없음)
            assertThat(result).hasSize(5)
            assertThat(result.map { it.date }).containsExactly(
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 6),
                LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 8),
                LocalDate.of(2026, 1, 9),
            )

            // 1/6(화)에는 출근 + 단건 = 2개
            val tuesday = result.find { it.date == LocalDate.of(2026, 1, 6) }
            assertThat(tuesday?.schedules).hasSize(2)
            assertThat(tuesday?.schedules?.map { it.title }).containsExactly("출근", "1/6 단건")

            // 나머지 날은 출근 1개만
            val monday = result.find { it.date == LocalDate.of(2026, 1, 5) }
            assertThat(monday?.schedules).hasSize(1)
            assertThat(monday?.schedules?.get(0)?.title).isEqualTo("출근")
        }

        @Test
        @DisplayName("여러 스케줄 중 일부만 exclusion으로 제외된다")
        fun partialExclusion_OnlyTargetRecordFiltered() {
            val date = LocalDate.of(2026, 1, 10)
            val schedule1 = ScheduleFixture.builder()
                .title("제외할 기록")
                .appointmentAt(date.atTime(10, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()
            val schedule2 = ScheduleFixture.builder()
                .title("유지할 기록")
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(9, 0)))
                .build()

            val exclusion = CalendarRecordExclusion.create(memberId, schedule1.id, date)
            stubMember()
            stubSchedulesWithExclusions(date, date, listOf(schedule1, schedule2), listOf(exclusion))

            val result = facade.getCalendarRange(memberId, date, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].schedules).hasSize(1)
            assertThat(result[0].schedules[0].title).isEqualTo("유지할 기록")
        }
    }

    @Nested
    @DisplayName("일별 조회")
    inner class DailyQuery {
        @Test
        @DisplayName("비반복 과거 스케줄의 상세 정보를 반환한다")
        fun nonRepeatPastSchedule_ReturnsDailyItem() {
            val date = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .title("과거 상세 일정")
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(date, date, listOf(schedule))

            val result = facade.getCalendarDaily(memberId, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].title).isEqualTo("과거 상세 일정")
            assertThat(result[0].type).isEqualTo(CalendarScheduleType.RECORD)
            assertThat(result[0].isRepeat).isFalse()
            assertThat(result[0].preparationNote).isEqualTo("준비 메모")
        }

        @Test
        @DisplayName("반복 스케줄이 해당 요일에 표시된다")
        fun repeatSchedule_ShownOnMatchingDay() {
            // 2026-03-15는 일요일 -> dayValue = 1
            val date = LocalDate.of(2026, 3, 15)
            val schedule = ScheduleFixture.builder()
                .title("매일 반복")
                .isRepeat(true)
                .repeatDays(ScheduleFixture.allDays())
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 3, 1), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(date, date, listOf(schedule))

            val result = facade.getCalendarDaily(memberId, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].isRepeat).isTrue()
        }

        @Test
        @DisplayName("소프트 삭제된 스케줄은 삭제 전 약속이면 표시된다")
        fun deletedSchedule_ShownBeforeDeletion() {
            val date = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .deletedAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 15), LocalTime.of(10, 0)))
                .build()

            stubMember()
            stubSchedules(date, date, listOf(schedule))

            val result = facade.getCalendarDaily(memberId, date)

            assertThat(result).hasSize(1)
            assertThat(result[0].type).isEqualTo(CalendarScheduleType.RECORD)
        }

        @Test
        @DisplayName("exclusion에 해당하는 과거 기록은 일별 조회에서도 제외된다")
        fun excludedRecord_IsFilteredInDaily() {
            val date = LocalDate.of(2026, 1, 10)
            val schedule = ScheduleFixture.builder()
                .appointmentAt(date.atTime(14, 0))
                .createdAt(ScheduleFixture.instantOf(LocalDate.of(2026, 1, 1), LocalTime.of(10, 0)))
                .build()

            val exclusion = CalendarRecordExclusion.create(memberId, schedule.id, date)
            stubMember()
            stubSchedulesWithExclusions(date, date, listOf(schedule), listOf(exclusion))

            val result = facade.getCalendarDaily(memberId, date)

            assertThat(result).isEmpty()
        }
    }
}
