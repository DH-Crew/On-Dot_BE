package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.Schedule
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional

interface ScheduleRepository : JpaRepository<Schedule, Long> {
    @EntityGraph(attributePaths = ["preparationAlarm", "departureAlarm"])
    fun findFirstByMemberIdAndDeletedAtIsNullOrderByUpdatedAtDesc(memberId: Long): Optional<Schedule>

    fun deleteByMemberId(memberId: Long)

    fun findAllByMemberId(memberId: Long): List<Schedule>

    @Query("SELECT s FROM Schedule s WHERE s.memberId IN :memberIds AND s.appointmentAt >= :start AND s.appointmentAt < :end AND s.deletedAt IS NULL")
    fun findAllByMemberIdInAndAppointmentAtRange(
        @Param("memberIds") memberIds: List<Long>,
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): List<Schedule>

    fun findAllByMemberIdInAndIsRepeatTrueAndDeletedAtIsNull(memberIds: List<Long>): List<Schedule>
}
