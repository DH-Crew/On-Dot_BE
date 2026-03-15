package com.dh.ondot.schedule.fixture

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import java.time.LocalDate

object CalendarRecordExclusionFixture {
    fun create(
        memberId: Long = 1L,
        scheduleId: Long = 1L,
        excludedDate: LocalDate = LocalDate.of(2026, 3, 14),
    ): CalendarRecordExclusion = CalendarRecordExclusion.create(memberId, scheduleId, excludedDate)
}
