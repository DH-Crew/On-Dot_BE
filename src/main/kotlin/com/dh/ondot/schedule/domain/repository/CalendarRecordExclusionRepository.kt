package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CalendarRecordExclusionRepository : JpaRepository<CalendarRecordExclusion, Long> {
    fun findAllByMemberIdAndExcludedDateBetween(
        memberId: Long, startDate: LocalDate, endDate: LocalDate,
    ): List<CalendarRecordExclusion>

    fun existsByMemberIdAndScheduleIdAndExcludedDate(
        memberId: Long, scheduleId: Long, excludedDate: LocalDate,
    ): Boolean
}
