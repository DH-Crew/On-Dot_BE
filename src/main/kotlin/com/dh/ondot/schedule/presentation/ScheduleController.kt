package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.presentation.request.AlarmSwitchRequest
import com.dh.ondot.schedule.presentation.request.EstimateTimeRequest
import com.dh.ondot.schedule.presentation.request.QuickScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleParsedRequest
import com.dh.ondot.schedule.presentation.request.ScheduleUpdateRequest
import com.dh.ondot.schedule.presentation.response.AlarmSwitchResponse
import com.dh.ondot.schedule.presentation.response.EstimateTimeResponse
import com.dh.ondot.schedule.presentation.response.HomeScheduleListResponse
import com.dh.ondot.schedule.presentation.response.ScheduleCreateResponse
import com.dh.ondot.schedule.presentation.response.ScheduleDetailResponse
import com.dh.ondot.schedule.presentation.response.ScheduleParsedResponse
import com.dh.ondot.schedule.presentation.response.SchedulePreparationResponse
import com.dh.ondot.schedule.presentation.response.ScheduleUpdateResponse
import com.dh.ondot.schedule.presentation.swagger.ScheduleSwagger
import com.dh.ondot.schedule.application.ScheduleCommandFacade
import com.dh.ondot.schedule.application.ScheduleQueryFacade
import com.dh.ondot.schedule.domain.service.RouteService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleQueryFacade: ScheduleQueryFacade,
    private val scheduleCommandFacade: ScheduleCommandFacade,
    private val routeService: RouteService,
) : ScheduleSwagger {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    override fun createSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleCreateRequest,
    ): ScheduleCreateResponse {
        val schedule = scheduleCommandFacade.createSchedule(memberId, request)

        return ScheduleCreateResponse.of(schedule)
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quick")
    override fun createQuickSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickSchedule(memberId, request)
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quickV1")
    fun createQuickScheduleV1(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickScheduleV1(memberId, request)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/voice")
    override fun parseVoiceSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleParsedRequest,
    ): ScheduleParsedResponse {
        return scheduleCommandFacade.parseVoiceSchedule(memberId, request.text)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/estimate-time")
    override fun estimateTravelTime(
        @Valid @RequestBody request: EstimateTimeRequest,
    ): EstimateTimeResponse {
        val estimatedTime = routeService.calculateRouteTime(
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
        )

        return EstimateTimeResponse.from(estimatedTime)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}")
    override fun getSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ): ScheduleDetailResponse {
        val schedule = scheduleQueryFacade.findOneByMemberAndSchedule(memberId, scheduleId)

        return ScheduleDetailResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/preparation")
    override fun getPreparationInfo(
        @PathVariable scheduleId: Long,
    ): SchedulePreparationResponse {
        val schedule = scheduleQueryFacade.findOne(scheduleId)

        return SchedulePreparationResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/issues")
    override fun getScheduleIssues(
        @PathVariable scheduleId: Long,
    ): String {
        return scheduleQueryFacade.getIssues(scheduleId)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    override fun getActiveSchedules(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): HomeScheduleListResponse {
        val pageable = PageRequest.of(page, size)
        return scheduleQueryFacade.findAllActiveSchedules(memberId, pageable)
    }

    @PutMapping("/{scheduleId}")
    override fun updateSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: ScheduleUpdateRequest,
    ): ResponseEntity<ScheduleUpdateResponse> {
        val result = scheduleCommandFacade.updateSchedule(memberId, scheduleId, request)
        val status = if (result.needsDepartureTimeRecalculation) HttpStatus.ACCEPTED else HttpStatus.OK

        return ResponseEntity.status(status).body(ScheduleUpdateResponse.of(result.schedule))
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{scheduleId}/alarm")
    override fun switchAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: AlarmSwitchRequest,
    ): AlarmSwitchResponse {
        val schedule = scheduleCommandFacade.switchAlarm(memberId, scheduleId, request.isEnabled)

        return AlarmSwitchResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ) {
        scheduleCommandFacade.deleteSchedule(memberId, scheduleId)
    }
}
