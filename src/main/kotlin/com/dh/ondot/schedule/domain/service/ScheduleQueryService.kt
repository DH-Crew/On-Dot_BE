package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import com.dh.ondot.schedule.infra.ScheduleQueryRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class ScheduleQueryService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleQueryRepository: ScheduleQueryRepository,
) {
    fun findScheduleById(id: Long): Schedule =
        scheduleRepository.findById(id)
            .orElseThrow { NotFoundScheduleException(id) }

    fun findScheduleByIdEager(scheduleId: Long): Schedule =
        scheduleQueryRepository.findScheduleById(scheduleId)
            .orElseThrow { NotFoundScheduleException(scheduleId) }

    fun findScheduleByMemberIdAndId(memberId: Long, scheduleId: Long): Schedule =
        scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId)
            .orElseThrow { NotFoundScheduleException(scheduleId) }

    fun getActiveSchedules(memberId: Long, page: Pageable): Slice<Schedule> {
        val now = TimeUtils.nowSeoulInstant()
        return scheduleQueryRepository.findActiveSchedulesByMember(memberId, now, page)
    }
}
