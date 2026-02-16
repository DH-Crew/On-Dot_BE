package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.application.command.GenerateAlarmCommand
import com.dh.ondot.schedule.presentation.request.RecordAlarmTriggerRequest
import com.dh.ondot.schedule.presentation.request.SetAlarmRequest
import com.dh.ondot.schedule.presentation.response.SettingAlarmResponse
import com.dh.ondot.schedule.presentation.swagger.AlarmSwagger
import com.dh.ondot.schedule.application.AlarmFacade
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
) : AlarmSwagger {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/setting")
    override fun setAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: SetAlarmRequest,
    ): SettingAlarmResponse {
        val command = GenerateAlarmCommand(
            request.appointmentAt,
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
        )
        val schedule = alarmFacade.generateAlarmSettingByRoute(memberId, command)

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
        alarmFacade.recordAlarmTrigger(
            memberId,
            request.alarmId,
            request.scheduleId,
            request.action,
            mobileType ?: "",
        )
    }
}
