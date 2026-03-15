package com.dh.ondot.schedule.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.dto.CalendarDailyItem
import com.dh.ondot.schedule.application.dto.CalendarDayItem
import com.dh.ondot.schedule.application.dto.CalendarScheduleItem
import com.dh.ondot.schedule.core.exception.CalendarDateRangeTooLargeException
import com.dh.ondot.schedule.core.exception.InvalidCalendarDateRangeException
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.infra.CalendarQueryRepository
import com.dh.ondot.schedule.presentation.response.AlarmDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class CalendarQueryFacade(
    private val memberService: MemberService,
    private val calendarQueryRepository: CalendarQueryRepository,
    private val exclusionService: CalendarRecordExclusionService,
) {
    companion object {
        private const val MAX_RANGE_DAYS = 45L
        private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")
    }

    fun getCalendarRange(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<CalendarDayItem> {
        validateDateRange(startDate, endDate)
        memberService.getMemberIfExists(memberId)

        val now = Instant.now()
        val rangeStart = startDate.atStartOfDay(SEOUL_ZONE).toInstant()
        val rangeEnd = endDate.plusDays(1).atStartOfDay(SEOUL_ZONE).toInstant()

        val schedules = calendarQueryRepository.findSchedulesForCalendarRange(memberId, rangeStart, rangeEnd)
        val exclusions = exclusionService.findExclusionsInRange(memberId, startDate, endDate)
        val excludedSet = exclusions.map { it.scheduleId to it.excludedDate }.toSet()

        val dayMap = mutableMapOf<LocalDate, MutableList<CalendarScheduleItem>>()

        for (schedule in schedules) {
            if (schedule.isRepeat) {
                expandRepeatSchedule(schedule, startDate, endDate, now, excludedSet, dayMap)
            } else {
                expandNonRepeatSchedule(schedule, startDate, endDate, now, excludedSet, dayMap)
            }
        }

        return dayMap.entries
            .sortedBy { it.key }
            .map { (date, items) -> CalendarDayItem(date, items.sortedBy { it.appointmentAt }) }
    }

    fun getCalendarDaily(memberId: Long, date: LocalDate): List<CalendarDailyItem> {
        memberService.getMemberIfExists(memberId)

        val now = Instant.now()
        val rangeStart = date.atStartOfDay(SEOUL_ZONE).toInstant()
        val rangeEnd = date.plusDays(1).atStartOfDay(SEOUL_ZONE).toInstant()

        val schedules = calendarQueryRepository.findSchedulesForCalendarRange(memberId, rangeStart, rangeEnd)
        val exclusions = exclusionService.findExclusionsInRange(memberId, date, date)
        val excludedSet = exclusions.map { it.scheduleId to it.excludedDate }.toSet()

        val items = mutableListOf<CalendarDailyItem>()

        for (schedule in schedules) {
            val appointmentTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalTime() ?: continue

            if (schedule.isRepeat) {
                if (!schedule.isScheduledForDayOfWeek(date)) continue
                val createdDate = schedule.createdAt?.atZone(SEOUL_ZONE)?.toLocalDate() ?: continue
                if (createdDate.isAfter(date)) continue

                val appointmentInstant = date.atTime(appointmentTime).atZone(SEOUL_ZONE).toInstant()
                val type = resolveScheduleType(schedule, appointmentInstant, date, now, excludedSet) ?: continue

                items.add(toDailyItem(schedule, type, date.atTime(appointmentTime)))
            } else {
                val appointmentDate = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalDate() ?: continue
                if (appointmentDate != date) continue

                val type = resolveScheduleType(schedule, schedule.appointmentAt, date, now, excludedSet) ?: continue
                val appointmentDateTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt) ?: continue
                items.add(toDailyItem(schedule, type, appointmentDateTime))
            }
        }

        return items.sortedBy { it.appointmentAt }
    }

    private fun expandRepeatSchedule(
        schedule: Schedule, startDate: LocalDate, endDate: LocalDate,
        now: Instant, excludedSet: Set<Pair<Long, LocalDate>>,
        dayMap: MutableMap<LocalDate, MutableList<CalendarScheduleItem>>,
    ) {
        val createdDate = schedule.createdAt?.atZone(SEOUL_ZONE)?.toLocalDate() ?: return
        val appointmentTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalTime() ?: return

        var date = if (startDate.isAfter(createdDate)) startDate else createdDate
        while (!date.isAfter(endDate)) {
            if (schedule.isScheduledForDayOfWeek(date)) {
                val appointmentInstant = date.atTime(appointmentTime).atZone(SEOUL_ZONE).toInstant()
                val type = resolveScheduleType(schedule, appointmentInstant, date, now, excludedSet)

                if (type != null) {
                    dayMap.getOrPut(date) { mutableListOf() }.add(
                        CalendarScheduleItem(
                            scheduleId = schedule.id,
                            title = schedule.title,
                            type = type,
                            isRepeat = true,
                            appointmentAt = date.atTime(appointmentTime),
                        )
                    )
                }
            }
            date = date.plusDays(1)
        }
    }

    private fun expandNonRepeatSchedule(
        schedule: Schedule, startDate: LocalDate, endDate: LocalDate,
        now: Instant, excludedSet: Set<Pair<Long, LocalDate>>,
        dayMap: MutableMap<LocalDate, MutableList<CalendarScheduleItem>>,
    ) {
        val appointmentDateTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt) ?: return
        val appointmentDate = appointmentDateTime.toLocalDate()

        if (appointmentDate.isBefore(startDate) || appointmentDate.isAfter(endDate)) return

        val type = resolveScheduleType(schedule, schedule.appointmentAt, appointmentDate, now, excludedSet) ?: return

        dayMap.getOrPut(appointmentDate) { mutableListOf() }.add(
            CalendarScheduleItem(
                scheduleId = schedule.id,
                title = schedule.title,
                type = type,
                isRepeat = false,
                appointmentAt = appointmentDateTime,
            )
        )
    }

    /**
     * 삭제 상태, 시간 기준 타입(RECORD/ALARM), exclusion 여부를 종합 판단하여 스케줄 타입을 반환한다.
     * 표시하지 않아야 할 스케줄이면 null을 반환한다.
     */
    private fun resolveScheduleType(
        schedule: Schedule, appointmentInstant: Instant, date: LocalDate,
        now: Instant, excludedSet: Set<Pair<Long, LocalDate>>,
    ): CalendarScheduleType? {
        if (schedule.isDeleted()) {
            val deletedAt = schedule.deletedAt ?: return null
            if (!deletedAt.isAfter(appointmentInstant)) return null
        }

        val type = if (appointmentInstant.isBefore(now)) CalendarScheduleType.RECORD else CalendarScheduleType.ALARM
        if (type == CalendarScheduleType.RECORD && (schedule.id to date) in excludedSet) return null

        return type
    }

    private fun toDailyItem(schedule: Schedule, type: CalendarScheduleType, appointmentAt: LocalDateTime): CalendarDailyItem =
        CalendarDailyItem(
            scheduleId = schedule.id,
            type = type,
            title = schedule.title,
            isRepeat = schedule.isRepeat,
            repeatDays = schedule.repeatDays?.toList() ?: emptyList(),
            appointmentAt = appointmentAt,
            preparationAlarm = schedule.preparationAlarm?.let { AlarmDto.of(it) },
            departureAlarm = schedule.departureAlarm?.let { AlarmDto.of(it) },
            hasActiveAlarm = (schedule.preparationAlarm?.isEnabled == true) || (schedule.departureAlarm?.isEnabled == true),
            startLongitude = schedule.departurePlace?.longitude,
            startLatitude = schedule.departurePlace?.latitude,
            endLongitude = schedule.arrivalPlace?.longitude,
            endLatitude = schedule.arrivalPlace?.latitude,
            preparationNote = schedule.preparationNote,
        )

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        if (startDate.isAfter(endDate)) {
            throw InvalidCalendarDateRangeException(startDate.toString(), endDate.toString())
        }
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (days > MAX_RANGE_DAYS) {
            throw CalendarDateRangeTooLargeException(days)
        }
    }
}
