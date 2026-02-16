package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.api.request.QuickScheduleCreateRequest
import com.dh.ondot.schedule.api.request.ScheduleCreateRequest
import com.dh.ondot.schedule.api.request.ScheduleUpdateRequest
import com.dh.ondot.schedule.api.response.ScheduleParsedResponse
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
    fun createSchedule(memberId: Long, request: ScheduleCreateRequest): Schedule {
        val departurePlace = Place.createPlace(
            request.departurePlace.title,
            request.departurePlace.roadAddress,
            request.departurePlace.longitude,
            request.departurePlace.latitude,
        )

        val arrivalPlace = Place.createPlace(
            request.arrivalPlace.title,
            request.arrivalPlace.roadAddress,
            request.arrivalPlace.longitude,
            request.arrivalPlace.latitude,
        )

        val preparationAlarm = Alarm.createPreparationAlarm(
            request.preparationAlarm.alarmMode,
            request.preparationAlarm.isEnabled,
            request.preparationAlarm.triggeredAt,
//                request.preparationAlarm.mission,
            request.preparationAlarm.isSnoozeEnabled,
            request.preparationAlarm.snoozeInterval,
            request.preparationAlarm.snoozeCount,
            request.preparationAlarm.soundCategory,
            request.preparationAlarm.ringTone,
            request.preparationAlarm.volume,
        )

        val departureAlarm = Alarm.createDepartureAlarm(
            request.departureAlarm.alarmMode,
            request.departureAlarm.triggeredAt,
            request.departureAlarm.isSnoozeEnabled,
            request.departureAlarm.snoozeInterval,
            request.departureAlarm.snoozeCount,
            request.departureAlarm.soundCategory,
            request.departureAlarm.ringTone,
            request.departureAlarm.volume,
        )

        val schedule = Schedule.createSchedule(
            memberId,
            departurePlace,
            arrivalPlace,
            preparationAlarm,
            departureAlarm,
            request.title,
            request.isRepeat,
            TreeSet(request.repeatDays),
            request.appointmentAt,
            request.isMedicationRequired,
            request.preparationNote,
        )

        return scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickSchedule(memberId: Long, request: QuickScheduleCreateRequest) {
        val member = memberService.getMemberIfExists(memberId)

        val dep = Place.createPlace(
            request.departurePlace.title,
            request.departurePlace.roadAddress,
            request.departurePlace.longitude,
            request.departurePlace.latitude,
        )
        val arr = Place.createPlace(
            request.arrivalPlace.title,
            request.arrivalPlace.roadAddress,
            request.arrivalPlace.longitude,
            request.arrivalPlace.latitude,
        )

        val estimatedTime = routeService.calculateRouteTime(
            request.departurePlace.longitude, request.departurePlace.latitude,
            request.arrivalPlace.longitude, request.arrivalPlace.latitude,
        )

        val schedule = scheduleService.setupSchedule(
            member, request.appointmentAt, estimatedTime,
        )
        schedule.registerPlaces(dep, arr)

        scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickScheduleV1(memberId: Long, request: QuickScheduleCreateRequest) {
        memberService.getMemberIfExists(memberId)
        val cmd = quickScheduleMapper.toCommand(memberId, request)
        val event = placeService.savePlaces(cmd)
        eventPublisher.publishEvent(event)
    }

    @Transactional
    fun updateSchedule(memberId: Long, scheduleId: Long, request: ScheduleUpdateRequest): UpdateScheduleResult {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)

        // 장소 변경 여부 확인
        val departureChanged = schedule.departurePlace!!.isPlaceChanged(
            request.departurePlace.roadAddress,
            request.departurePlace.longitude,
            request.departurePlace.latitude,
        )

        val arrivalChanged = schedule.arrivalPlace!!.isPlaceChanged(
            request.arrivalPlace.roadAddress,
            request.arrivalPlace.longitude,
            request.arrivalPlace.latitude,
        )

        val placeChanged = departureChanged || arrivalChanged
        val timeChanged = schedule.isAppointmentTimeChanged(request.appointmentAt)

        // 장소가 달라졌다면 → (비동기) 새로운 시간 계산 후 처리 (TODO 주석으로 남김)
        if (placeChanged || timeChanged) {
            // TODO: 경로 재계산·도착/출발 시간 보정 등의 비동기 로직 호출
        }

        schedule.departurePlace!!.update(
            request.departurePlace.title,
            request.departurePlace.roadAddress,
            request.departurePlace.longitude,
            request.departurePlace.latitude,
        )

        schedule.arrivalPlace!!.update(
            request.arrivalPlace.title,
            request.arrivalPlace.roadAddress,
            request.arrivalPlace.longitude,
            request.arrivalPlace.latitude,
        )

        schedule.preparationAlarm!!.updatePreparation(
            request.preparationAlarm.alarmMode,
            request.preparationAlarm.isEnabled,
            request.preparationAlarm.triggeredAt,
//                request.preparationAlarm.mission,
            request.preparationAlarm.isSnoozeEnabled,
            request.preparationAlarm.snoozeInterval,
            request.preparationAlarm.snoozeCount,
            request.preparationAlarm.soundCategory,
            request.preparationAlarm.ringTone,
            request.preparationAlarm.volume,
        )

        schedule.departureAlarm!!.updateDeparture(
            request.departureAlarm.alarmMode,
            request.departureAlarm.triggeredAt,
            request.departureAlarm.isSnoozeEnabled,
            request.departureAlarm.snoozeInterval,
            request.departureAlarm.snoozeCount,
            request.departureAlarm.soundCategory,
            request.departureAlarm.ringTone,
            request.departureAlarm.volume,
        )

        schedule.updateCore(
            request.title,
            request.isRepeat,
            TreeSet(request.repeatDays),
            request.appointmentAt,
        )

        return UpdateScheduleResult(schedule, placeChanged || timeChanged)
    }

    @Transactional
    fun parseVoiceSchedule(memberId: Long, sentence: String): ScheduleParsedResponse {
        memberService.getMemberIfExists(memberId)
        aiUsageService.increaseUsage(memberId)
        return openAiPromptApi.parseNaturalLanguage(sentence)
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
