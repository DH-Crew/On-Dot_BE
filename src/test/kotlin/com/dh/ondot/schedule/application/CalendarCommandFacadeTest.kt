package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import com.dh.ondot.schedule.fixture.ScheduleFixture
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarCommandFacade 테스트")
class CalendarCommandFacadeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var scheduleQueryService: ScheduleQueryService
    @Mock private lateinit var exclusionService: CalendarRecordExclusionService

    @InjectMocks private lateinit var facade: CalendarCommandFacade

    @Test
    @DisplayName("캘린더 기록 삭제 시 소유권 검증 후 제외 기록을 생성한다")
    fun deleteCalendarRecord_VerifiesOwnershipAndExcludes() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()
        given(scheduleQueryService.findScheduleByMemberIdAndIdIncludingDeleted(1L, 1L))
            .willReturn(schedule)

        // when
        facade.deleteCalendarRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        // then
        verify(memberService).getMemberIfExists(1L)
        verify(exclusionService).excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))
    }
}
