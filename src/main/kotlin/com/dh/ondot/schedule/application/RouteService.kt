package com.dh.ondot.schedule.application

import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.RouteTimeCalculator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RouteService(
    private val calculators: List<RouteTimeCalculator>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
        appointmentAt: LocalDateTime? = null,
    ): Int {
        val supportedCalculators = calculators.filter { it.supports(transportType) }
        if (supportedCalculators.isEmpty()) {
            throw IllegalArgumentException("지원하지 않는 교통수단입니다: $transportType")
        }

        var lastException: Exception? = null
        for (calculator in supportedCalculators) {
            try {
                return calculator.calculateRouteTime(startX, startY, endX, endY, appointmentAt)
            } catch (e: Exception) {
                lastException = e
                log.warn("${calculator.javaClass.simpleName} 실패, 다음 계산기로 폴백: ${e.message}")
            }
        }
        throw lastException ?: IllegalStateException("경로 시간 계산에 실패했습니다: $transportType")
    }
}
