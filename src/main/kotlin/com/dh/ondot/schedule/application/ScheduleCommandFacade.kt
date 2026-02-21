package com.dh.ondot.schedule.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.application.command.UpdateScheduleCommand
import com.dh.ondot.schedule.application.dto.ScheduleParsedResult
import com.dh.ondot.schedule.application.dto.UpdateScheduleResult
import com.dh.ondot.schedule.application.mapper.QuickScheduleMapper
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.*
import com.dh.ondot.schedule.infra.api.EverytimeApi
import com.dh.ondot.schedule.infra.api.OpenAiPromptApi
import com.dh.ondot.schedule.infra.exception.EverytimeEmptyTimetableException
import com.dh.ondot.schedule.infra.exception.EverytimeInvalidUrlException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.TreeSet

@Service
class ScheduleCommandFacade(
    private val memberService: MemberService,
    private val scheduleService: ScheduleService,
    private val scheduleQueryService: ScheduleQueryService,
    private val routeService: RouteService,
    private val placeService: PlaceService,
    private val aiUsageService: AiUsageService,
    private val quickScheduleMapper: QuickScheduleMapper,
    private val everytimeApi: EverytimeApi,
    private val openAiPromptApi: OpenAiPromptApi,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun createSchedule(memberId: Long, command: CreateScheduleCommand): Schedule {
        val departurePlace = Place.createPlace(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        val arrivalPlace = Place.createPlace(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val preparationAlarm = Alarm.createPreparationAlarm(
            command.preparationAlarm.alarmMode,
            command.preparationAlarm.isEnabled,
            command.preparationAlarm.triggeredAt,
            command.preparationAlarm.isSnoozeEnabled,
            command.preparationAlarm.snoozeInterval,
            command.preparationAlarm.snoozeCount,
            command.preparationAlarm.soundCategory,
            command.preparationAlarm.ringTone,
            command.preparationAlarm.volume,
        )

        val departureAlarm = Alarm.createDepartureAlarm(
            command.departureAlarm.alarmMode,
            command.departureAlarm.triggeredAt,
            command.departureAlarm.isSnoozeEnabled,
            command.departureAlarm.snoozeInterval,
            command.departureAlarm.snoozeCount,
            command.departureAlarm.soundCategory,
            command.departureAlarm.ringTone,
            command.departureAlarm.volume,
        )

        val schedule = Schedule.createSchedule(
            memberId,
            departurePlace,
            arrivalPlace,
            preparationAlarm,
            departureAlarm,
            command.title,
            command.isRepeat,
            TreeSet(command.repeatDays),
            command.appointmentAt,
            command.isMedicationRequired,
            command.preparationNote,
            command.transportType,
        )

        return scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickSchedule(memberId: Long, command: CreateQuickScheduleCommand) {
        val member = memberService.getMemberIfExists(memberId)

        val dep = Place.createPlace(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )
        val arr = Place.createPlace(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val estimatedTime = routeService.calculateRouteTime(
            command.departurePlace.longitude, command.departurePlace.latitude,
            command.arrivalPlace.longitude, command.arrivalPlace.latitude,
            appointmentAt = command.appointmentAt,
        )

        val schedule = scheduleService.setupSchedule(
            member, command.appointmentAt, estimatedTime,
        )
        schedule.registerPlaces(dep, arr)

        scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickScheduleV1(memberId: Long, command: CreateQuickScheduleCommand) {
        memberService.getMemberIfExists(memberId)
        val cmd = quickScheduleMapper.toCommand(memberId, command)
        val event = placeService.savePlaces(cmd)
        eventPublisher.publishEvent(event)
    }

    @Transactional
    fun updateSchedule(memberId: Long, scheduleId: Long, command: UpdateScheduleCommand): UpdateScheduleResult {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)

        val departureChanged = schedule.departurePlace!!.isPlaceChanged(
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        val arrivalChanged = schedule.arrivalPlace!!.isPlaceChanged(
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val placeChanged = departureChanged || arrivalChanged
        val timeChanged = schedule.isAppointmentTimeChanged(command.appointmentAt)

        if (placeChanged || timeChanged) {
            // TODO: 경로 재계산·도착/출발 시간 보정 등의 비동기 로직 호출
        }

        schedule.departurePlace!!.update(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        schedule.arrivalPlace!!.update(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        schedule.preparationAlarm!!.updatePreparation(
            command.preparationAlarm.alarmMode,
            command.preparationAlarm.isEnabled,
            command.preparationAlarm.triggeredAt,
            command.preparationAlarm.isSnoozeEnabled,
            command.preparationAlarm.snoozeInterval,
            command.preparationAlarm.snoozeCount,
            command.preparationAlarm.soundCategory,
            command.preparationAlarm.ringTone,
            command.preparationAlarm.volume,
        )

        schedule.departureAlarm!!.updateDeparture(
            command.departureAlarm.alarmMode,
            command.departureAlarm.triggeredAt,
            command.departureAlarm.isSnoozeEnabled,
            command.departureAlarm.snoozeInterval,
            command.departureAlarm.snoozeCount,
            command.departureAlarm.soundCategory,
            command.departureAlarm.ringTone,
            command.departureAlarm.volume,
        )

        schedule.updateCore(
            command.title,
            command.isRepeat,
            TreeSet(command.repeatDays),
            command.appointmentAt,
        )

        return UpdateScheduleResult(schedule, placeChanged || timeChanged)
    }

    @Transactional
    fun parseVoiceSchedule(memberId: Long, sentence: String): ScheduleParsedResult {
        memberService.getMemberIfExists(memberId)
        aiUsageService.increaseUsage(memberId)
        val parsed = openAiPromptApi.parseNaturalLanguage(sentence)
        return ScheduleParsedResult(parsed.departurePlaceTitle, parsed.appointmentAt)
    }

    @Transactional
    fun switchAlarm(
        memberId: Long, scheduleId: Long, enabled: Boolean,
    ): Schedule {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)
        schedule.switchAlarm(enabled)
        return schedule
    }

    fun deleteSchedule(memberId: Long, scheduleId: Long) {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)
        scheduleService.deleteSchedule(schedule)
    }

    fun validateEverytimeUrl(url: String): String {
        val identifier = extractIdentifier(url)
        everytimeApi.fetchTimetable(identifier)
        return identifier
    }

    @Transactional
    fun createSchedulesFromEverytime(
        memberId: Long,
        url: String,
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        transportType: TransportType,
    ): List<Schedule> {
        val member = memberService.getMemberIfExists(memberId)
        val identifier = extractIdentifier(url)
        val lectures = everytimeApi.fetchTimetable(identifier)

        if (lectures.isEmpty()) {
            throw EverytimeEmptyTimetableException()
        }

        // 요일별 첫 수업 시작시간 추출
        val firstLectureByDay: Map<Int, LocalTime> = lectures
            .groupBy { it.day }
            .mapValues { (_, dayLectures) -> dayLectures.minOf { it.startTime } }

        // 동일 시간으로 그룹핑 (월→일 순서)
        val timeGroups: Map<LocalTime, List<Int>> = firstLectureByDay.entries
            .groupBy({ it.value }, { it.key })
            .mapValues { (_, days) -> days.sortedBy { it } }

        // 경로 계산
        val routeTimeByGroup = calculateRouteTimeByGroup(
            timeGroups, startX, startY, endX, endY, transportType,
        )

        // 그룹별 Schedule + Alarm 생성
        return timeGroups.map { (time, days) ->
            val title = buildScheduleTitle(days)
            val repeatDays = days.map { everytimeDayToRepeatDay(it) }.toSortedSet()
            val appointmentAt = calculateNextAppointmentAt(days, time)
            val estimatedTime = routeTimeByGroup[time] ?: 0

            val schedule = scheduleService.setupSchedule(member, appointmentAt, estimatedTime)
            schedule.memberId = member.id
            schedule.title = title
            schedule.isRepeat = true
            schedule.repeatDays = TreeSet(repeatDays)
            schedule.appointmentAt = TimeUtils.toInstant(appointmentAt)
            schedule.transportType = transportType

            scheduleService.saveSchedule(schedule)
        }
    }

    private fun extractIdentifier(url: String): String {
        try {
            val uri = URI(url)
            if (uri.host != "everytime.kr") {
                throw EverytimeInvalidUrlException(url)
            }
            val path = uri.path
            if (!path.startsWith("/@")) {
                throw EverytimeInvalidUrlException(url)
            }
            val identifier = path.removePrefix("/@")
            if (identifier.isBlank()) {
                throw EverytimeInvalidUrlException(url)
            }
            return identifier
        } catch (e: EverytimeInvalidUrlException) {
            throw e
        } catch (_: Exception) {
            throw EverytimeInvalidUrlException(url)
        }
    }

    private fun calculateRouteTimeByGroup(
        timeGroups: Map<LocalTime, List<Int>>,
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        transportType: TransportType,
    ): Map<LocalTime, Int> {
        if (transportType == TransportType.PUBLIC_TRANSPORT) {
            val routeTime = routeService.calculateRouteTime(startX, startY, endX, endY, transportType)
            return timeGroups.keys.associateWith { routeTime }
        }

        return timeGroups.map { (time, days) ->
            val representativeDay = days.first()
            val appointmentAt = calculateNextAppointmentAt(listOf(representativeDay), time)
            val routeTime = routeService.calculateRouteTime(
                startX, startY, endX, endY, transportType, appointmentAt,
            )
            time to routeTime
        }.toMap()
    }

    private fun buildScheduleTitle(days: List<Int>): String {
        val dayNames = mapOf(
            0 to "월", 1 to "화", 2 to "수", 3 to "목",
            4 to "금", 5 to "토", 6 to "일",
        )
        val dayStr = days.joinToString("/") { dayNames[it] ?: "" }
        return "${dayStr}요일 학교"
    }

    private fun everytimeDayToRepeatDay(everytimeDay: Int): Int =
        when (everytimeDay) {
            0 -> 2  // 월
            1 -> 3  // 화
            2 -> 4  // 수
            3 -> 5  // 목
            4 -> 6  // 금
            5 -> 7  // 토
            6 -> 1  // 일
            else -> throw IllegalArgumentException("잘못된 에브리타임 요일: $everytimeDay")
        }

    private fun calculateNextAppointmentAt(days: List<Int>, time: LocalTime): LocalDateTime {
        val today = TimeUtils.nowSeoulDate()
        val now = TimeUtils.nowSeoulDateTime()
        val targetDaysOfWeek = days.map { everytimeDayToDayOfWeek(it) }

        for (daysAhead in 0..7L) {
            val candidateDate = today.plusDays(daysAhead)
            if (candidateDate.dayOfWeek in targetDaysOfWeek) {
                val candidateDateTime = candidateDate.atTime(time)
                if (candidateDateTime.isAfter(now)) {
                    return candidateDateTime
                }
            }
        }
        return today.plusDays(7).atTime(time)
    }

    private fun everytimeDayToDayOfWeek(everytimeDay: Int): DayOfWeek =
        when (everytimeDay) {
            0 -> DayOfWeek.MONDAY
            1 -> DayOfWeek.TUESDAY
            2 -> DayOfWeek.WEDNESDAY
            3 -> DayOfWeek.THURSDAY
            4 -> DayOfWeek.FRIDAY
            5 -> DayOfWeek.SATURDAY
            6 -> DayOfWeek.SUNDAY
            else -> throw IllegalArgumentException("잘못된 에브리타임 요일: $everytimeDay")
        }
}
