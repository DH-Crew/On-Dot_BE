package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.GeoUtils
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.infra.api.OdsayPathApi
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse
import com.dh.ondot.schedule.infra.exception.OdsayTooCloseException
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class RouteService(
    private val odayPathApi: OdsayPathApi,
    private val apiUsageService: ApiUsageService,
) {
    fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int {
        checkApiUsageLimit()
        val response = getRouteTimeFromApi(startX, startY, endX, endY)
        return calculateFinalTravelTime(response)
    }

    private fun checkApiUsageLimit() {
        apiUsageService.checkAndIncrementUsage(ApiType.ODSAY)
    }

    private fun getRouteTimeFromApi(startX: Double, startY: Double, endX: Double, endY: Double): OdsayRouteApiResponse {
        return try {
            odayPathApi.searchPublicTransportRoute(startX, startY, endX, endY)
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

    /**
     * 모든 경로에 대해 보정된 시간 계산
     */
    private fun calculateAdjustedTimesForAllPaths(response: OdsayRouteApiResponse): List<Double> =
        response.result!!
            .path!!.stream()
            .map { calculateAdjustedTimeForSinglePath(it) }
            .sorted()
            .limit(TOP_ROUTES_LIMIT.toLong())
            .toList()

    /**
     * 단일 경로에 대한 시간 보정 계산
     */
    private fun calculateAdjustedTimeForSinglePath(path: OdsayRouteApiResponse.Path): Double {
        val baseTime = path.info.totalTime
        val transferPenalty = calculateTransferPenalty(path)
        val longWalkPenalty = calculateLongWalkPenalty(path)

        return baseTime + transferPenalty + longWalkPenalty
    }

    /**
     * 환승 시간 페널티 계산 (환승 1회당 6.5분 추가)
     */
    private fun calculateTransferPenalty(path: OdsayRouteApiResponse.Path): Double {
        val publicTransportLegs = countPublicTransportLegs(path)
        val transferCount = maxOf(0L, publicTransportLegs - 1)
        return transferCount * TRANSFER_PENALTY_MINUTES
    }

    /**
     * 긴 도보 구간 페널티 계산 (800m 초과 도보당 4분 추가)
     */
    private fun calculateLongWalkPenalty(path: OdsayRouteApiResponse.Path): Double {
        val longWalkCount = countLongWalkSegments(path)
        return longWalkCount * LONG_WALK_PENALTY_MINUTES
    }

    /**
     * 대중교통 구간 개수 계산 (지하철, 버스)
     */
    private fun countPublicTransportLegs(path: OdsayRouteApiResponse.Path): Long =
        path.subPath.stream()
            .filter { it.trafficType == SUBWAY_TRAFFIC_TYPE || it.trafficType == BUS_TRAFFIC_TYPE }
            .count()

    /**
     * 긴 도보 구간 개수 계산 (800m 초과)
     */
    private fun countLongWalkSegments(path: OdsayRouteApiResponse.Path): Long =
        path.subPath.stream()
            .filter { it.trafficType == WALKING_TRAFFIC_TYPE && it.distance > LONG_WALK_DISTANCE_THRESHOLD }
            .count()

    /**
     * 상위 경로들의 평균 시간 계산
     */
    private fun calculateAverageOfTopRoutes(adjustedTimes: List<Double>): Double =
        adjustedTimes.stream()
            .mapToDouble { it }
            .average()
            .orElse(0.0)

    /**
     * 여유시간 추가 후 반올림
     */
    private fun addBufferTimeAndRound(averageTime: Double): Int =
        round((averageTime + BUFFER_TIME_MINUTES) * BUFFER_TIME_RATIO).toInt()

    /**
     * 거리를 도보 시간으로 변환
     */
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
