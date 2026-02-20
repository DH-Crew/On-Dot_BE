package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.enums.TransportType
import org.springframework.stereotype.Service

@Service
class RouteService(
    private val calculators: List<RouteTimeCalculator>,
) {
    fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
    ): Int {
        val calculator = calculators.first { it.supports(transportType) }
        return calculator.calculateRouteTime(startX, startY, endX, endY)
    }
}
