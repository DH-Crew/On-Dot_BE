package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.Schedule
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ScheduleRepository : JpaRepository<Schedule, Long> {
    @EntityGraph(attributePaths = ["preparationAlarm", "departureAlarm"])
    fun findFirstByMemberIdOrderByUpdatedAtDesc(memberId: Long): Optional<Schedule>

    fun deleteByMemberId(memberId: Long)
}
