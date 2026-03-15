package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import com.dh.ondot.schedule.domain.repository.CalendarRecordExclusionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CalendarRecordExclusionService(
    private val repository: CalendarRecordExclusionRepository,
) {
    fun findExclusionsInRange(
        memberId: Long, startDate: LocalDate, endDate: LocalDate,
    ): List<CalendarRecordExclusion> =
        repository.findAllByMemberIdAndExcludedDateBetween(memberId, startDate, endDate)

    @Transactional
    fun excludeRecord(memberId: Long, scheduleId: Long, excludedDate: LocalDate) {
        if (repository.existsByMemberIdAndScheduleIdAndExcludedDate(memberId, scheduleId, excludedDate)) {
            return
        }
        try {
            repository.save(CalendarRecordExclusion.create(memberId, scheduleId, excludedDate))
        } catch (_: DataIntegrityViolationException) {
            // 동시 요청으로 인한 유니크 키 충돌 — 멱등성 보장을 위해 무시
        }
    }
}
