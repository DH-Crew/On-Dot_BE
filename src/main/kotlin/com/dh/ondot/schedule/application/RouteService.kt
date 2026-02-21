package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.RouteTimeCalculator
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RouteService(
    private val calculators: List<RouteTimeCalculator>,
) {
    fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
        appointmentAt: LocalDateTime? = null,
    ): Int {
        val calculator = calculators.firstOrNull { it.supports(transportType) }
            ?: throw IllegalArgumentException("지원하지 않는 교통수단입니다: $transportType")
        return calculator.calculateRouteTime(startX, startY, endX, endY, appointmentAt)
    }
}
