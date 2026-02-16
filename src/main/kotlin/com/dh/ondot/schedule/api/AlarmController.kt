package com.dh.ondot.schedule.api

import com.dh.ondot.schedule.api.request.RecordAlarmTriggerRequest
import com.dh.ondot.schedule.api.request.SetAlarmRequest
import com.dh.ondot.schedule.api.response.SettingAlarmResponse
import com.dh.ondot.schedule.api.swagger.AlarmSwagger
import com.dh.ondot.schedule.application.AlarmFacade
import com.dh.ondot.schedule.domain.service.AlarmService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/alarms")
class AlarmController(
    private val alarmFacade: AlarmFacade,
    private val alarmService: AlarmService,
) : AlarmSwagger {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/setting")
    override fun setAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: SetAlarmRequest,
    ): SettingAlarmResponse {
        val schedule = alarmFacade.generateAlarmSettingByRoute(
            memberId, request.appointmentAt,
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
        )

        return SettingAlarmResponse.from(
            schedule.preparationAlarm!!,
            schedule.departureAlarm!!,
        )
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/triggers")
    override fun recordAlarmTrigger(
        @RequestAttribute("memberId") memberId: Long,
        @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String?,
        @Valid @RequestBody request: RecordAlarmTriggerRequest,
    ) {
        alarmService.recordTrigger(
            memberId,
            request.alarmId,
            request.scheduleId,
            request.action,
            mobileType ?: "",
        )
    }
}
