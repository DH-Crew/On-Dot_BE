package com.dh.ondot.schedule.application

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
import com.dh.ondot.schedule.domain.service.*
import com.dh.ondot.schedule.infra.api.OpenAiPromptApi
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
}
