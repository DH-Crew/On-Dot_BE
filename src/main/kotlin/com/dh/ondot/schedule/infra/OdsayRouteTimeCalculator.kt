package com.dh.ondot.schedule.infra

import com.dh.ondot.core.util.GeoUtils
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.ApiUsageService
import com.dh.ondot.schedule.domain.service.RouteTimeCalculator
import com.dh.ondot.schedule.infra.api.OdsayPathApi
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse
import com.dh.ondot.schedule.infra.exception.OdsayTooCloseException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.math.round

@Component
class OdsayRouteTimeCalculator(
    private val odsayPathApi: OdsayPathApi,
    private val apiUsageService: ApiUsageService,
) : RouteTimeCalculator {

    override fun supports(transportType: TransportType): Boolean =
        transportType == TransportType.PUBLIC_TRANSPORT

    override fun calculateRouteTime(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        appointmentAt: LocalDateTime?,
    ): Int {
        apiUsageService.checkAndIncrementUsage(ApiType.ODSAY)
        val response = getRouteTimeFromApi(startX, startY, endX, endY)
        return calculateFinalTravelTime(response)
    }

    private fun getRouteTimeFromApi(startX: Double, startY: Double, endX: Double, endY: Double): OdsayRouteApiResponse {
        return try {
            odsayPathApi.searchPublicTransportRoute(startX, startY, endX, endY)
        } catch (e: OdsayTooCloseException) {
            val walkTime = calculateWalkTime(startX, startY, endX, endY)
            OdsayRouteApiResponse.walkOnly(walkTime)
        }
    }

    private fun calculateWalkTime(startX: Double, startY: Double, endX: Double, endY: Double): Int {
        val distanceInMeters = GeoUtils.calculateDistance(startX, startY, endX, endY)
        return convertDistanceToWalkingTime(distanceInMeters)
    }

    private fun calculateFinalTravelTime(response: OdsayRouteApiResponse): Int {
        val adjustedTimes = calculateAdjustedTimesForAllPaths(response)
        val averageTime = calculateAverageOfTopRoutes(adjustedTimes)
        return addBufferTimeAndRound(averageTime)
    }

    private fun calculateAdjustedTimesForAllPaths(response: OdsayRouteApiResponse): List<Double> {
        val paths = response.result?.path
            ?: throw IllegalStateException("경로 응답에 result 또는 path가 없습니다")
        return paths
            .map { calculateAdjustedTimeForSinglePath(it) }
            .sorted()
            .take(TOP_ROUTES_LIMIT)
    }

    private fun calculateAdjustedTimeForSinglePath(path: OdsayRouteApiResponse.Path): Double {
        val baseTime = path.info.totalTime
        val transferPenalty = calculateTransferPenalty(path)
        val longWalkPenalty = calculateLongWalkPenalty(path)
        return baseTime + transferPenalty + longWalkPenalty
    }

    private fun calculateTransferPenalty(path: OdsayRouteApiResponse.Path): Double {
        val publicTransportLegs = path.subPath
            .count { it.trafficType == SUBWAY_TRAFFIC_TYPE || it.trafficType == BUS_TRAFFIC_TYPE }
        val transferCount = maxOf(0, publicTransportLegs - 1)
        return transferCount * TRANSFER_PENALTY_MINUTES
    }

    private fun calculateLongWalkPenalty(path: OdsayRouteApiResponse.Path): Double {
        val longWalkCount = path.subPath
            .count { it.trafficType == WALKING_TRAFFIC_TYPE && it.distance > LONG_WALK_DISTANCE_THRESHOLD }
        return longWalkCount * LONG_WALK_PENALTY_MINUTES
    }

    private fun calculateAverageOfTopRoutes(adjustedTimes: List<Double>): Double =
        adjustedTimes.average().takeIf { !it.isNaN() } ?: 0.0

    private fun addBufferTimeAndRound(averageTime: Double): Int =
        round((averageTime + BUFFER_TIME_MINUTES) * BUFFER_TIME_RATIO).toInt()

    private fun convertDistanceToWalkingTime(distanceInMeters: Double): Int {
        val timeInSeconds = distanceInMeters / WALKING_SPEED_MPS
        val timeInMinutes = timeInSeconds / 60
        return round(timeInMinutes).toInt()
    }

    companion object {
        private const val TRANSFER_PENALTY_MINUTES = 6.5
        private const val LONG_WALK_PENALTY_MINUTES = 4.0
        private const val LONG_WALK_DISTANCE_THRESHOLD = 800
        private const val BUFFER_TIME_MINUTES = 5
        private const val BUFFER_TIME_RATIO = 1.07
        private const val TOP_ROUTES_LIMIT = 3
        private const val WALKING_SPEED_MPS = 1.25
        private const val SUBWAY_TRAFFIC_TYPE = 1
        private const val BUS_TRAFFIC_TYPE = 2
        private const val WALKING_TRAFFIC_TYPE = 3
    }
}
