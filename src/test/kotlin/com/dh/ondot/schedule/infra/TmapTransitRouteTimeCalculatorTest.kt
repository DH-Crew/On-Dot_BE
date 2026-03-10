package com.dh.ondot.schedule.infra

import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.ApiUsageService
import com.dh.ondot.schedule.infra.api.TmapTransitPathApi
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse.*
import com.dh.ondot.schedule.infra.exception.TmapTransitNoRouteException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.math.ceil

class TmapTransitRouteTimeCalculatorTest {

    private lateinit var tmapTransitPathApi: TmapTransitPathApi
    private lateinit var apiUsageService: ApiUsageService
    private lateinit var calculator: TmapTransitRouteTimeCalculator

    @BeforeEach
    fun setUp() {
        tmapTransitPathApi = mock(TmapTransitPathApi::class.java)
        apiUsageService = mock(ApiUsageService::class.java)
        calculator = TmapTransitRouteTimeCalculator(tmapTransitPathApi, apiUsageService)
    }

    @Test
    fun `supports는 PUBLIC_TRANSPORT에 대해 true를 반환한다`() {
        assertTrue(calculator.supports(TransportType.PUBLIC_TRANSPORT))
    }

    @Test
    fun `supports는 CAR에 대해 false를 반환한다`() {
        assertEquals(false, calculator.supports(TransportType.CAR))
    }

    @Test
    fun `상위 3개 경로 평균 + 10분 버퍼로 시간을 계산한다`() {
        // itineraries: 600초(10분), 900초(15분), 1200초(20분), 1800초(30분)
        // 상위 3개: 10, 15, 20 → 평균 15 + 10 = 25분
        val response = createResponse(listOf(600, 900, 1200, 1800))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        assertEquals(25, result)
        verify(apiUsageService).checkAndIncrementUsage(ApiType.TMAP_TRANSIT)
    }

    @Test
    fun `경로가 3개 미만이면 전체 경로로 평균을 계산한다`() {
        // itineraries: 600초(10분), 1200초(20분) → 평균 15 + 10 = 25분
        val response = createResponse(listOf(600, 1200))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        assertEquals(25, result)
    }

    @Test
    fun `소수점은 올림 처리한다`() {
        // itineraries: 610초(10.17분), 920초(15.33분), 1230초(20.5분)
        // 상위 3개 평균: 15.33 + 10 = 25.33 → ceil → 26분
        val response = createResponse(listOf(610, 920, 1230))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        val expectedAvg = listOf(610, 920, 1230).map { it / 60.0 }.average()
        val expected = ceil(expectedAvg + 10).toInt()
        assertEquals(expected, result)
    }

    @Test
    fun `TmapTransitNoRouteException 발생 시 도보 시간으로 폴백한다`() {
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
            .thenThrow(TmapTransitNoRouteException("거리가 가까움"))

        // 같은 좌표 → 거리 0 → 도보 시간 0분 → 0 + 10 = 10분
        val result = calculator.calculateRouteTime(127.0, 37.0, 127.0, 37.0)

        assertEquals(10, result)
    }

    private fun createResponse(totalTimesInSeconds: List<Int>): TmapTransitRouteApiResponse {
        val itineraries = totalTimesInSeconds.map { totalTime ->
            Itinerary(
                totalTime = totalTime,
                transferCount = 0,
                walkDistance = 0.0,
                walkTime = 0,
                totalDistance = 0.0,
                pathType = 0,
            )
        }
        return TmapTransitRouteApiResponse(
            metaData = MetaData(
                plan = Plan(itineraries = itineraries)
            )
        )
    }
}
