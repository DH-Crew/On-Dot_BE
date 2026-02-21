package com.dh.ondot.schedule.domain.service

import com.dh.ondot.member.domain.Member
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.util.SortedSet

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
) {
    fun createScheduleWithAlarms(
        member: Member, appointmentAt: LocalDateTime, estimatedTimeMin: Int,
    ): Schedule {
        val schedule = scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id)

        return if (schedule.isPresent) {
            createFromLatestUserSetting(schedule.get(), member, appointmentAt, estimatedTimeMin)
        } else {
            Schedule.createWithDefaultAlarmSetting(
                member.defaultAlarmMode!!, member.snooze!!, member.sound!!,
                appointmentAt, estimatedTimeMin, member.preparationTime!!,
            )
        }
    }

    fun setupSchedule(
        member: Member, appointmentAt: LocalDateTime, estimatedTimeMin: Int,
    ): Schedule {
        val newSchedule = createScheduleWithAlarms(member, appointmentAt, estimatedTimeMin)
        newSchedule.setupQuickSchedule(member.id, appointmentAt)
        return newSchedule
    }

    fun createEverytimeSchedule(
        member: Member, departurePlace: Place, arrivalPlace: Place,
        title: String, repeatDays: SortedSet<Int>, appointmentAt: LocalDateTime,
        estimatedTimeMin: Int, transportType: TransportType,
    ): Schedule {
        val base = createScheduleWithAlarms(member, appointmentAt, estimatedTimeMin)
        return saveSchedule(Schedule.createSchedule(
            memberId = member.id,
            departurePlace = departurePlace,
            arrivalPlace = arrivalPlace,
            preparationAlarm = base.preparationAlarm!!,
            departureAlarm = base.departureAlarm!!,
            title = title,
            isRepeat = true,
            repeatDays = repeatDays,
            appointmentAt = appointmentAt,
            transportType = transportType,
        ))
    }

    private fun createFromLatestUserSetting(
        latestSchedule: Schedule, member: Member,
        appointment: LocalDateTime, estimatedTimeMin: Int,
    ): Schedule {
        val copy = copySchedule(latestSchedule)
        copy.preparationAlarm!!.changeEnabled(true)
        copy.departureAlarm!!.changeEnabled(true)
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
