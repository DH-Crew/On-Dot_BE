package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.infra.api.TmapPathApi
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class TmapRouteTimeCalculator(
    private val tmapPathApi: TmapPathApi,
    private val apiUsageService: ApiUsageService,
) : RouteTimeCalculator {

    override fun supports(transportType: TransportType): Boolean =
        transportType == TransportType.CAR

    override fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int {
        apiUsageService.checkAndIncrementUsage(ApiType.TMAP)
        val response = tmapPathApi.searchCarRoute(startX, startY, endX, endY)
        val totalTimeSeconds = response.getTotalTimeSeconds()
        val totalTimeMinutes = ceil(totalTimeSeconds / 60.0).toInt()
        return totalTimeMinutes + BUFFER_TIME_MINUTES
    }

    companion object {
        private const val BUFFER_TIME_MINUTES = 10
    }
}
