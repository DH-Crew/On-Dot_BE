package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.enums.TransportType
import java.time.LocalDateTime

interface RouteTimeCalculator {
    fun supports(transportType: TransportType): Boolean
    fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        appointmentAt: LocalDateTime? = null,
    ): Int
}
