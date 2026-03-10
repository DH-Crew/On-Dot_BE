# TMAP 대중교통 API 마이그레이션 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 대중교통 예상 시간 계산을 ODSAY에서 TMAP 요약정보 API(`/transit/routes/sub`)로 교체한다.

**Architecture:** 새로운 `TmapTransitRouteTimeCalculator`를 생성하여 Strategy 패턴으로 `PUBLIC_TRANSPORT` 계산기를 교체한다. 기존 ODSAY 코드는 유지하되, `@Primary`로 TMAP을 우선 사용한다. API 사용량은 `ApiType.TMAP_TRANSIT`으로 별도 추적한다.

**Tech Stack:** Kotlin, Spring Boot, Spring RestClient, Spring Retry, JPA, JUnit5/Mockito

---

### Task 1: ErrorCode 및 ApiType 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt:82-85`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/enums/ApiType.kt:3-6`

**Step 1: ApiType에 TMAP_TRANSIT 추가**

```kotlin
// src/main/kotlin/com/dh/ondot/schedule/domain/enums/ApiType.kt
package com.dh.ondot.schedule.domain.enums

enum class ApiType {
    ODSAY,
    TMAP,
    TMAP_TRANSIT,
}
```

**Step 2: ErrorCode에 TMAP Transit 에러 코드 추가**

`// TMAP API` 섹션 아래에 추가:

```kotlin
    // TMAP Transit API
    TMAP_TRANSIT_NO_ROUTE(BAD_REQUEST, "대중교통 경로를 찾을 수 없습니다: %s"),
    TMAP_TRANSIT_BAD_INPUT(BAD_REQUEST, "대중교통 API 입력값 형식 및 범위를 확인해주세요: %s"),
    TMAP_TRANSIT_MISSING_PARAM(BAD_REQUEST, "대중교통 API 필수 입력값이 누락되었습니다: %s"),
    TMAP_TRANSIT_SERVICE_AREA(BAD_REQUEST, "대중교통 서비스 지역이 아닙니다: %s"),
    TMAP_TRANSIT_SERVER_ERROR(BAD_GATEWAY, "TMAP 대중교통 서버 오류가 발생했습니다: %s"),
    TMAP_TRANSIT_UNHANDLED_ERROR(INTERNAL_SERVER_ERROR, "TMAP 대중교통 API 처리 중 알 수 없는 오류가 발생했습니다: %s"),
```

**Step 3: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt \
        src/main/kotlin/com/dh/ondot/schedule/domain/enums/ApiType.kt
git commit -m "refactor: TMAP Transit API용 ErrorCode 및 ApiType 추가 (#DH-89)"
```

---

### Task 2: 예외 클래스 생성

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitNoRouteException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitBadInputException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitMissingParamException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitServiceAreaException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitServerErrorException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransitUnhandledException.kt`

**Step 1: 6개 예외 클래스 생성**

기존 패턴을 따른다 (예: `TmapServerErrorException`).

```kotlin
// TmapTransitNoRouteException.kt — 에러 코드 11, 12, 13, 14 (도보 폴백 트리거)
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_NO_ROUTE

class TmapTransitNoRouteException(detail: String) :
    BadRequestException(TMAP_TRANSIT_NO_ROUTE.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_NO_ROUTE.name
}
```

```kotlin
// TmapTransitBadInputException.kt — 에러 코드 21
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_BAD_INPUT

class TmapTransitBadInputException(detail: String) :
    BadRequestException(TMAP_TRANSIT_BAD_INPUT.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_BAD_INPUT.name
}
```

```kotlin
// TmapTransitMissingParamException.kt — 에러 코드 22
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_MISSING_PARAM

class TmapTransitMissingParamException(detail: String) :
    BadRequestException(TMAP_TRANSIT_MISSING_PARAM.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_MISSING_PARAM.name
}
```

```kotlin
// TmapTransitServiceAreaException.kt — 에러 코드 23
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_SERVICE_AREA

class TmapTransitServiceAreaException(detail: String) :
    BadRequestException(TMAP_TRANSIT_SERVICE_AREA.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_SERVICE_AREA.name
}
```

```kotlin
// TmapTransitServerErrorException.kt — 에러 코드 31 (재시도 트리거)
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_SERVER_ERROR

class TmapTransitServerErrorException(detail: String) :
    BadGatewayException(TMAP_TRANSIT_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_SERVER_ERROR.name
}
```

```kotlin
// TmapTransitUnhandledException.kt — 에러 코드 32 및 기타
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.InternalServerException
import com.dh.ondot.core.exception.ErrorCode.TMAP_TRANSIT_UNHANDLED_ERROR

class TmapTransitUnhandledException(detail: String) :
    InternalServerException(TMAP_TRANSIT_UNHANDLED_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_TRANSIT_UNHANDLED_ERROR.name
}
```

**Step 2: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapTransit*.kt
git commit -m "refactor: TMAP Transit 예외 클래스 6종 추가 (#DH-89)"
```

---

### Task 3: 응답 DTO 생성

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/dto/TmapTransitRouteApiResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/dto/TmapTransitErrorResponse.kt`

**Step 1: 정상 응답 DTO**

```kotlin
// TmapTransitRouteApiResponse.kt
package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapTransitRouteApiResponse(
    val metaData: MetaData,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MetaData(
        val plan: Plan,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Plan(
        val itineraries: List<Itinerary>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Itinerary(
        val totalTime: Int,
        val transferCount: Int,
        val walkDistance: Double,
        val walkTime: Int,
        val totalDistance: Double,
        val pathType: Int,
    )
}
```

**Step 2: 에러 응답 DTO**

```kotlin
// TmapTransitErrorResponse.kt
package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapTransitErrorResponse(
    val result: Result?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Result(
        val status: Int,
        val message: String,
    )
}
```

**Step 3: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/dto/TmapTransit*.kt
git commit -m "refactor: TMAP Transit 응답 DTO 추가 (#DH-89)"
```

---

### Task 4: API 클라이언트 생성 (TmapTransitPathApi)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/api/TmapTransitPathApi.kt`

**Ref:** 기존 `TmapPathApi.kt`와 `OdsayPathApi.kt` 패턴 참고

**Step 1: TmapTransitPathApi 구현**

```kotlin
package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.infra.dto.TmapTransitErrorResponse
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse
import com.dh.ondot.schedule.infra.exception.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class TmapTransitPathApi(
    @Qualifier("tmapRestClient") private val tmapRestClient: RestClient,
    private val objectMapper: ObjectMapper,
) {

    @Retryable(
        retryFor = [TmapTransitServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchTransitRoute(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
    ): TmapTransitRouteApiResponse {
        try {
            val rawBody = tmapRestClient.post()
                .uri("/transit/routes/sub")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf(
                    "startX" to startX.toString(),
                    "startY" to startY.toString(),
                    "endX" to endX.toString(),
                    "endY" to endY.toString(),
                    "count" to 10,
                    "lang" to 0,
                    "format" to "json",
                ))
                .retrieve()
                .body(String::class.java)

            if (rawBody == null || rawBody.isBlank()) {
                throw TmapTransitUnhandledException("TMAP Transit API 응답이 null 또는 비어 있습니다.")
            }

            // 에러 응답 체크: "result" 필드가 있고 "status" 필드가 있으면 에러
            if (rawBody.contains("\"result\"") && rawBody.contains("\"status\"")) {
                val errorResponse = objectMapper.readValue(rawBody, TmapTransitErrorResponse::class.java)
                val result = errorResponse.result
                if (result != null) {
                    throwExceptionByErrorCode(result.status, result.message)
                }
            }

            val response = objectMapper.readValue(rawBody, TmapTransitRouteApiResponse::class.java)

            if (response.metaData.plan.itineraries.isEmpty()) {
                throw TmapTransitNoRouteException("출발지($startX,$startY) → 도착지($endX,$endY) 경로 없음")
            }

            return response
        } catch (ex: TmapTransitServerErrorException) {
            throw ex
        } catch (ex: TmapTransitNoRouteException) {
            throw ex
        } catch (ex: TmapTransitBadInputException) {
            throw ex
        } catch (ex: TmapTransitMissingParamException) {
            throw ex
        } catch (ex: TmapTransitServiceAreaException) {
            throw ex
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.is5xxServerError) {
                throw TmapTransitServerErrorException("${ex.statusCode}: ${ex.message}")
            }
            throw TmapTransitUnhandledException("${ex.statusCode}: ${ex.message}")
        } catch (e: Exception) {
            throw TmapTransitUnhandledException("${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun throwExceptionByErrorCode(status: Int, message: String) {
        when (status) {
            11, 12, 13, 14 -> throw TmapTransitNoRouteException(message)
            21 -> throw TmapTransitBadInputException(message)
            22 -> throw TmapTransitMissingParamException(message)
            23 -> throw TmapTransitServiceAreaException(message)
            31 -> throw TmapTransitServerErrorException(message)
            32 -> throw TmapTransitUnhandledException(message)
            else -> throw TmapTransitUnhandledException("Unknown error code $status: $message")
        }
    }
}
```

**Step 2: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/api/TmapTransitPathApi.kt
git commit -m "refactor: TMAP Transit API 클라이언트 구현 (#DH-89)"
```

---

### Task 5: Calculator 구현 (TmapTransitRouteTimeCalculator)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/TmapTransitRouteTimeCalculator.kt`

**Ref:** 기존 `OdsayRouteTimeCalculator.kt` 패턴 참고. 주요 차이: 패널티/1.07x 없음, totalTime이 초 단위

**Step 1: Calculator 구현**

```kotlin
package com.dh.ondot.schedule.infra

import com.dh.ondot.core.util.GeoUtils
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.service.ApiUsageService
import com.dh.ondot.schedule.domain.service.RouteTimeCalculator
import com.dh.ondot.schedule.infra.api.TmapTransitPathApi
import com.dh.ondot.schedule.infra.dto.TmapTransitRouteApiResponse
import com.dh.ondot.schedule.infra.exception.TmapTransitNoRouteException
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.math.round

@Primary
@Component
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
```

**핵심 포인트:**
- `@Primary`: `OdsayRouteTimeCalculator`와 같은 `PUBLIC_TRANSPORT`를 지원하므로, `RouteService`의 `calculators.firstOrNull { it.supports(transportType) }`에서 `@Primary`가 우선 선택됨
- `totalTime / 60.0`: 초→분 변환
- `ceil(averageTime + 5)`: 평균 + 5분 버퍼, 올림
- 도보 폴백: `TmapTransitNoRouteException` catch 시 도보 시간으로 합성 응답 생성

**Step 2: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/TmapTransitRouteTimeCalculator.kt
git commit -m "refactor: TMAP Transit 경로 시간 계산기 구현 (#DH-89)"
```

---

### Task 6: 단위 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/schedule/infra/TmapTransitRouteTimeCalculatorTest.kt`

**Step 1: 테스트 작성**

```kotlin
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
    fun `상위 3개 경로 평균 + 5분 버퍼로 시간을 계산한다`() {
        // itineraries: 600초(10분), 900초(15분), 1200초(20분), 1800초(30분)
        // 상위 3개: 10, 15, 20 → 평균 15 + 5 = 20분
        val response = createResponse(listOf(600, 900, 1200, 1800))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        assertEquals(20, result)
        verify(apiUsageService).checkAndIncrementUsage(ApiType.TMAP_TRANSIT)
    }

    @Test
    fun `경로가 3개 미만이면 전체 경로로 평균을 계산한다`() {
        // itineraries: 600초(10분), 1200초(20분) → 평균 15 + 5 = 20분
        val response = createResponse(listOf(600, 1200))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        assertEquals(20, result)
    }

    @Test
    fun `소수점은 올림 처리한다`() {
        // itineraries: 610초(10.17분), 920초(15.33분), 1230초(20.5분)
        // 상위 3개 평균: 15.33 + 5 = 20.33 → ceil → 21분
        val response = createResponse(listOf(610, 920, 1230))
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(response)

        val result = calculator.calculateRouteTime(127.0, 37.0, 127.1, 37.1)

        val expectedAvg = listOf(610, 920, 1230).map { it / 60.0 }.average()
        val expected = ceil(expectedAvg + 5).toInt()
        assertEquals(expected, result)
    }

    @Test
    fun `TmapTransitNoRouteException 발생 시 도보 시간으로 폴백한다`() {
        `when`(tmapTransitPathApi.searchTransitRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
            .thenThrow(TmapTransitNoRouteException("거리가 가까움"))

        // 같은 좌표 → 거리 0 → 도보 시간 0분 → 0 + 5 = 5분
        val result = calculator.calculateRouteTime(127.0, 37.0, 127.0, 37.0)

        assertEquals(5, result)
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
```

**Step 2: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.infra.TmapTransitRouteTimeCalculatorTest"`
Expected: 5 tests PASSED

**Step 3: Commit**

```bash
git add src/test/kotlin/com/dh/ondot/schedule/infra/TmapTransitRouteTimeCalculatorTest.kt
git commit -m "test: TMAP Transit 경로 시간 계산기 단위 테스트 추가 (#DH-89)"
```

---

### Task 7: 전체 테스트 실행 및 검증

**Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 2: `@Primary` 충돌 확인**

`RouteService`는 `calculators: List<RouteTimeCalculator>`를 주입받고 `firstOrNull { it.supports(transportType) }`으로 선택한다. `@Primary`는 Spring DI에서 단일 빈 주입 시 우선순위를 결정하지만, `List` 주입에는 영향이 없다. `List`에서의 순서는 Spring이 보장하지 않으므로, `firstOrNull`이 ODSAY를 먼저 반환할 수 있다.

**해결 방안**: `TmapTransitRouteTimeCalculator`에 `@Order(1)`을 추가하고 `OdsayRouteTimeCalculator`에 `@Order(2)`를 추가하여 리스트 순서를 보장한다. 또는 `@Primary` 대신 `@Order`만 사용한다.

```kotlin
// TmapTransitRouteTimeCalculator.kt
@Component
@Order(1)
class TmapTransitRouteTimeCalculator(...) : RouteTimeCalculator { ... }
```

```kotlin
// OdsayRouteTimeCalculator.kt — 기존 파일에 @Order(2) 추가
@Component
@Order(2)
class OdsayRouteTimeCalculator(...) : RouteTimeCalculator { ... }
```

Modify: `src/main/kotlin/com/dh/ondot/schedule/infra/OdsayRouteTimeCalculator.kt:15` — `@Component` 위 또는 아래에 `@Order(2)` 추가. import 추가: `import org.springframework.core.annotation.Order`

**Step 3: 컴파일 및 전체 테스트**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/TmapTransitRouteTimeCalculator.kt \
        src/main/kotlin/com/dh/ondot/schedule/infra/OdsayRouteTimeCalculator.kt
git commit -m "refactor: @Order로 PUBLIC_TRANSPORT 계산기 우선순위 설정 (#DH-89)"
```

---

### Task 8: 최종 검증

**Step 1: 전체 빌드**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: 변경 파일 목록 확인**

Run: `git diff --stat develop...HEAD`

예상 변경 파일:
- `docs/plans/2026-03-08-tmap-transit-migration-design.md` (새 파일)
- `docs/plans/2026-03-08-tmap-transit-migration-plan.md` (새 파일)
- `src/main/kotlin/.../core/exception/ErrorCode.kt` (수정)
- `src/main/kotlin/.../schedule/domain/enums/ApiType.kt` (수정)
- `src/main/kotlin/.../schedule/infra/OdsayRouteTimeCalculator.kt` (수정 — @Order 추가)
- `src/main/kotlin/.../schedule/infra/TmapTransitRouteTimeCalculator.kt` (새 파일)
- `src/main/kotlin/.../schedule/infra/api/TmapTransitPathApi.kt` (새 파일)
- `src/main/kotlin/.../schedule/infra/dto/TmapTransitRouteApiResponse.kt` (새 파일)
- `src/main/kotlin/.../schedule/infra/dto/TmapTransitErrorResponse.kt` (새 파일)
- `src/main/kotlin/.../schedule/infra/exception/TmapTransit*.kt` (새 파일 6개)
- `src/test/kotlin/.../schedule/infra/TmapTransitRouteTimeCalculatorTest.kt` (새 파일)
