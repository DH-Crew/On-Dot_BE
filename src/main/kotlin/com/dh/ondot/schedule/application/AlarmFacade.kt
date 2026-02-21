package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.application.command.GenerateAlarmCommand
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.service.AlarmService
import com.dh.ondot.schedule.domain.service.RouteService
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlarmFacade(
    private val memberService: MemberService,
    private val routeService: RouteService,
    private val scheduleService: ScheduleService,
    private val alarmService: AlarmService,
) {
    @Transactional
    fun generateAlarmSettingByRoute(memberId: Long, command: GenerateAlarmCommand): Schedule {
        val member = memberService.getMemberIfExists(memberId)

        val estimatedTimeMin = routeService.calculateRouteTime(
            command.startLongitude, command.startLatitude,
            command.endLongitude, command.endLatitude,
            command.transportType,
        )

        return scheduleService.setupSchedule(
            member, command.appointmentAt, estimatedTimeMin,
        )
    }

    fun recordAlarmTrigger(
        memberId: Long,
        alarmId: Long,
        scheduleId: Long,
        action: String,
        mobileType: String,
    ) {
        alarmService.recordTrigger(memberId, alarmId, scheduleId, action, mobileType)
    }
}
