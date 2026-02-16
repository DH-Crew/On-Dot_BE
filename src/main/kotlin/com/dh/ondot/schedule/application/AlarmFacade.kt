package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.service.RouteService
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AlarmFacade(
    private val memberService: MemberService,
    private val routeService: RouteService,
    private val scheduleService: ScheduleService,
) {
    @Transactional
    fun generateAlarmSettingByRoute(
        memberId: Long, appointmentAt: LocalDateTime,
        startX: Double, startY: Double, endX: Double, endY: Double,
    ): Schedule {
        val member = memberService.getMemberIfExists(memberId)

        val estimatedTimeMin = routeService.calculateRouteTime(
            startX, startY,
            endX, endY,
        )

        return scheduleService.setupSchedule(
            member, appointmentAt, estimatedTimeMin,
        )
    }
}
