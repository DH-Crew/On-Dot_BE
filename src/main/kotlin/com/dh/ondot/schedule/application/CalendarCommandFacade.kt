package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CalendarCommandFacade(
    private val memberService: MemberService,
    private val scheduleQueryService: ScheduleQueryService,
    private val exclusionService: CalendarRecordExclusionService,
) {
    @Transactional
    fun deleteCalendarRecord(memberId: Long, scheduleId: Long, date: LocalDate) {
        memberService.getMemberIfExists(memberId)
        scheduleQueryService.findScheduleByMemberIdAndIdIncludingDeleted(memberId, scheduleId)
        exclusionService.excludeRecord(memberId, scheduleId, date)
    }
}
