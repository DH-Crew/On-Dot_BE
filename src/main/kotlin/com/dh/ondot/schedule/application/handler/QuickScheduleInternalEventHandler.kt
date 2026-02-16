package com.dh.ondot.schedule.application.handler

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent
import com.dh.ondot.schedule.domain.repository.PlaceRepository
import com.dh.ondot.schedule.domain.service.RouteService
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class QuickScheduleInternalEventHandler(
    private val memberService: MemberService,
    private val routeService: RouteService,
    private val scheduleService: ScheduleService,
    private val placeRepository: PlaceRepository,
) {
    @Transactional
    fun handleEvent(event: QuickScheduleRequestedEvent) {
        val member = memberService.getMemberIfExists(event.memberId)
        val dep = placeRepository.getReferenceById(event.departurePlaceId)
        val arr = placeRepository.getReferenceById(event.arrivalPlaceId)

        val estimatedTimeMin = routeService.calculateRouteTime(
            dep.longitude, dep.latitude,
            arr.longitude, arr.latitude,
        )

        val schedule = scheduleService.setupSchedule(
            member, event.appointmentAt, estimatedTimeMin,
        )
        schedule.registerPlaces(dep, arr)

        scheduleService.saveSchedule(schedule)
    }
}
