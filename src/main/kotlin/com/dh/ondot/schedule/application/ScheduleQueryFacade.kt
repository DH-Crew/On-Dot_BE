package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.domain.service.EmergencyAlertService
import com.dh.ondot.schedule.presentation.response.*
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem
import com.dh.ondot.schedule.application.mapper.HomeScheduleListItemMapper
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import java.time.LocalDateTime
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ScheduleQueryFacade(
    private val memberService: MemberService,
    private val scheduleService: ScheduleService,
    private val scheduleQueryService: ScheduleQueryService,
    private val emergencyAlertService: EmergencyAlertService,
    private val homeScheduleListItemMapper: HomeScheduleListItemMapper,
    private val routeService: RouteService,
) {
    fun findOne(scheduleId: Long): Schedule {
        return scheduleQueryService.findScheduleById(scheduleId)
    }

    fun findOneByMemberAndSchedule(memberId: Long, scheduleId: Long): Schedule {
        memberService.getMemberIfExists(memberId)
        return scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId)
    }

    fun findAllActiveSchedules(memberId: Long, page: Pageable): HomeScheduleListResponse {
        memberService.getMemberIfExists(memberId)

        val scheduleSlice = scheduleQueryService.getActiveSchedules(memberId, page)
        val scheduleItems = homeScheduleListItemMapper.toListOrderedByAlarmPriority(scheduleSlice.content)
        val earliestActiveAlarmAt = scheduleService.getEarliestActiveAlarmAt(scheduleSlice.content)
        val earliestAlarmId = findEarliestActiveAlarmScheduleId(scheduleItems)

        return HomeScheduleListResponse.of(earliestAlarmId, earliestActiveAlarmAt, scheduleItems, scheduleSlice.hasNext())
    }

    private fun findEarliestActiveAlarmScheduleId(sortedItems: List<HomeScheduleListItem>): Long? {
        return sortedItems.stream()
            .filter { it.hasActiveAlarm }
            .findFirst()
            .map { it.scheduleId }
            .orElse(null)
    }

    fun getIssues(scheduleId: Long): String {
        val schedule = scheduleQueryService.findScheduleByIdEager(scheduleId)
        val roadAddress = schedule.arrivalPlace!!.roadAddress
        // todo: 출발지 기반 긴급 알림, 지하철 알림 추가
        return emergencyAlertService.getIssuesByAddress(roadAddress)
    }

    @Transactional
    fun estimateTravelTime(
        startLongitude: Double, startLatitude: Double,
        endLongitude: Double, endLatitude: Double,
        transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
        appointmentAt: LocalDateTime? = null,
    ): Int {
        return routeService.calculateRouteTime(
            startLongitude, startLatitude, endLongitude, endLatitude,
            transportType, appointmentAt,
        )
    }
}
