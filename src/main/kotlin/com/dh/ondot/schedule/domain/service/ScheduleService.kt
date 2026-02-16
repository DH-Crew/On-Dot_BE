package com.dh.ondot.schedule.domain.service

import com.dh.ondot.member.domain.Member
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
) {
    fun setupSchedule(
        member: Member, appointmentAt: LocalDateTime, estimatedTimeMin: Int,
    ): Schedule {
        val schedule = scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id)

        val newSchedule = if (schedule.isPresent) {
            createFromLatestUserSetting(schedule.get(), member, appointmentAt, estimatedTimeMin)
        } else {
            Schedule.createWithDefaultAlarmSetting(
                member.defaultAlarmMode!!, member.snooze!!, member.sound!!,
                appointmentAt, estimatedTimeMin, member.preparationTime!!,
            )
        }
        newSchedule.setupQuickSchedule(member.id, appointmentAt)

        return newSchedule
    }

    private fun createFromLatestUserSetting(
        latestSchedule: Schedule, member: Member,
        appointment: LocalDateTime, estimatedTimeMin: Int,
    ): Schedule {
        val copy = copySchedule(latestSchedule)
        val depAlarmAt = appointment.minusMinutes(estimatedTimeMin.toLong())
        val prepAlarmAt = depAlarmAt.minusMinutes(member.preparationTime!!.toLong())
        copy.departureAlarm!!.updateTriggeredAt(depAlarmAt)
        copy.preparationAlarm!!.updateTriggeredAt(prepAlarmAt)

        return copy
    }

    private fun copySchedule(original: Schedule): Schedule =
        Schedule(
            preparationAlarm = original.preparationAlarm!!.copy(),
            departureAlarm = original.departureAlarm!!.copy(),
        )

    fun getEarliestActiveAlarmAt(schedules: List<Schedule>): Instant? =
        schedules
            .filter { it.hasAnyActiveAlarm() }
            .mapNotNull { it.calculateNextAlarmAt() }
            .minOrNull()

    @Transactional
    fun saveSchedule(schedule: Schedule): Schedule =
        scheduleRepository.save(schedule)

    @Transactional
    fun deleteSchedule(schedule: Schedule) {
        scheduleRepository.delete(schedule)
    }

    @Transactional
    fun deleteAllByMemberId(memberId: Long) {
        scheduleRepository.deleteByMemberId(memberId)
    }
}
