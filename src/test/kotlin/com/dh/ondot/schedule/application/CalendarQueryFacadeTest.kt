package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.core.exception.CalendarDateRangeTooLargeException
import com.dh.ondot.schedule.core.exception.InvalidCalendarDateRangeException
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.infra.CalendarQueryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarQueryFacade 테스트")
class CalendarQueryFacadeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var calendarQueryRepository: CalendarQueryRepository
    @Mock private lateinit var exclusionService: CalendarRecordExclusionService

    @InjectMocks private lateinit var facade: CalendarQueryFacade

    @Nested
    @DisplayName("날짜 범위 검증")
    inner class DateRangeValidation {
        @Test
        @DisplayName("startDate > endDate이면 예외 발생")
        fun invalidRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(1L, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 1))
            }.isInstanceOf(InvalidCalendarDateRangeException::class.java)
        }

        @Test
        @DisplayName("45일 초과 범위이면 예외 발생")
        fun tooLargeRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1))
            }.isInstanceOf(CalendarDateRangeTooLargeException::class.java)
        }
    }

    @Nested
    @DisplayName("범위 조회")
    inner class RangeQuery {
        @Test
        @DisplayName("스케줄이 없으면 빈 리스트를 반환한다")
        fun noSchedules_ReturnsEmpty() {
            // given
            val startDate = LocalDate.of(2026, 3, 1)
            val endDate = LocalDate.of(2026, 3, 31)
            given(calendarQueryRepository.findSchedulesForCalendarRange(
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.any(Instant::class.java),
                ArgumentMatchers.any(Instant::class.java),
            )).willReturn(emptyList())
            given(exclusionService.findExclusionsInRange(1L, startDate, endDate)).willReturn(emptyList())

            // when
            val result = facade.getCalendarRange(1L, startDate, endDate)

            // then
            assertThat(result).isEmpty()
        }
    }
}
