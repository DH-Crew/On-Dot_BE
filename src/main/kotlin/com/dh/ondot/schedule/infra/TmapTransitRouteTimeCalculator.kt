package com.dh.ondot.schedule.infra

import com.dh.ondot.core.util.GeoUtils
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.ApiUsageService
import com.dh.ondot.schedule.domain.service.RouteTimeCalculator
import com.dh.ondot.schedule.infra.api.TmapTransitPathApi
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse
import com.dh.ondot.schedule.infra.exception.TmapTransitNoRouteException
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.math.round

@Component
@Order(1)
class TmapTransitRouteTimeCalculator(
    private val tmapTransitPathApi: TmapTransitPathApi,
    private val apiUsageService: ApiUsageService,
) : RouteTimeCalculator {

    override fun supports(transportType: TransportType): Boolean =
        transportType == TransportType.PUBLIC_TRANSPORT

    override fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        appointmentAt: LocalDateTime?,
    ): Int {
        apiUsageService.checkAndIncrementUsage(ApiType.TMAP_TRANSIT)
        val response = getRouteTimeFromApi(startX, startY, endX, endY)
        return calculateFinalTravelTime(response.metaData.plan.itineraries)
    }

    private fun getRouteTimeFromApi(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
    ): TmapTransitRouteApiResponse {
        return try {
            tmapTransitPathApi.searchTransitRoute(startX, startY, endX, endY)
        } catch (e: TmapTransitNoRouteException) {
            val walkTimeMinutes = calculateWalkTime(startX, startY, endX, endY)
            TmapTransitRouteApiResponse(
                metaData = TmapTransitRouteApiResponse.MetaData(
                    plan = TmapTransitRouteApiResponse.Plan(
                        itineraries = listOf(
                            TmapTransitRouteApiResponse.Itinerary(
                                totalTime = walkTimeMinutes * 60,
                                transferCount = 0,
                                walkDistance = 0.0,
                                walkTime = walkTimeMinutes * 60,
                                totalDistance = 0.0,
                                pathType = 0,
                            )
                        )
                    )
                )
            )
        }
    }

    private fun calculateWalkTime(startX: Double, startY: Double, endX: Double, endY: Double): Int {
        val distanceInMeters = GeoUtils.calculateDistance(startX, startY, endX, endY)
        val timeInSeconds = distanceInMeters / WALKING_SPEED_MPS
        val timeInMinutes = timeInSeconds / 60
        return round(timeInMinutes).toInt()
    }

    private fun calculateFinalTravelTime(itineraries: List<TmapTransitRouteApiResponse.Itinerary>): Int {
        val timesInMinutes = itineraries
            .map { it.totalTime / 60.0 }
            .sorted()
            .take(TOP_ROUTES_LIMIT)

        val averageTime = timesInMinutes.average().takeIf { !it.isNaN() } ?: 0.0
        return ceil(averageTime + BUFFER_TIME_MINUTES).toInt()
    }

    companion object {
        private const val BUFFER_TIME_MINUTES = 5
        private const val TOP_ROUTES_LIMIT = 3
        private const val WALKING_SPEED_MPS = 1.25
    }
}
