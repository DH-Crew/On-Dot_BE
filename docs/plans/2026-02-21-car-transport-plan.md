# 자가용 기능 구현 (DH-23) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** TMAP API를 활용한 자가용 경로 시간 계산 기능을 추가하고, Strategy 패턴으로 기존 ODSAY 로직을 리팩토링한다.

**Architecture:** RouteTimeCalculator 인터페이스를 도입하여 ODSAY/TMAP 경로 계산을 분리. ApiUsage 통합 엔티티로 API 사용량을 일반화. 기존 estimate-time, set-alarm, schedule-create API에 선택적 transportType 파라미터를 추가.

**Tech Stack:** Kotlin, Spring Boot 3, Spring RestClient, Spring Retry, JPA/Hibernate, TMAP API

**Design Doc:** `docs/plans/2026-02-21-car-transport-design.md`

---

## Task 1: TransportType, ApiType enum 생성

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/enums/TransportType.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/enums/ApiType.kt`

**Step 1: TransportType enum 생성**

```kotlin
package com.dh.ondot.schedule.domain.enums

enum class TransportType {
    PUBLIC_TRANSPORT,
    CAR,
}
```

**Step 2: ApiType enum 생성**

```kotlin
package com.dh.ondot.schedule.domain.enums

enum class ApiType {
    ODSAY,
    TMAP,
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```
feat: TransportType, ApiType enum 추가
```

---

## Task 2: ApiUsage 통합 (OdsayUsage 리네이밍)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/OdsayUsage.kt` → `ApiUsage.kt`로 이름 변경
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/repository/OdsayUsageRepository.kt` → `ApiUsageRepository.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/service/OdsayUsageService.kt` → `ApiUsageService.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/core/exception/MaxOdsayUsageLimitExceededException.kt` → `MaxApiUsageLimitExceededException.kt`
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt` (line 75)
- Modify: `src/test/kotlin/com/dh/ondot/schedule/domain/service/OdsayUsageServiceTest.kt` → `ApiUsageServiceTest.kt`

**Step 1: OdsayUsage → ApiUsage 엔티티 변경**

`OdsayUsage.kt` 파일을 `ApiUsage.kt`로 이름 변경하고 내용 수정:

```kotlin
package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.schedule.domain.enums.ApiType
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "api_usages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["api_type", "usage_date"])
    ]
)
class ApiUsage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false)
    val apiType: ApiType,

    @Column(name = "usage_date", nullable = false)
    val usageDate: LocalDate,

    @Column(name = "count", nullable = false)
    var count: Int,
) : BaseTimeEntity() {

    fun getRemainingUsage(): Int = maxOf(0, DAILY_LIMIT - count)

    companion object {
        const val DAILY_LIMIT = 1000

        @JvmStatic
        fun newForToday(apiType: ApiType, date: LocalDate): ApiUsage =
            ApiUsage(apiType = apiType, usageDate = date, count = 1)
    }
}
```

**Step 2: OdsayUsageRepository → ApiUsageRepository 변경**

```kotlin
package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.ApiUsage
import com.dh.ondot.schedule.domain.enums.ApiType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface ApiUsageRepository : JpaRepository<ApiUsage, Long> {

    fun findByApiTypeAndUsageDate(apiType: ApiType, usageDate: LocalDate): Optional<ApiUsage>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE ApiUsage a
        SET a.count = a.count + 1
        WHERE a.apiType = :apiType
            AND a.usageDate = :usageDate
            AND a.count < 1000
        """
    )
    fun incrementUsageCount(@Param("apiType") apiType: ApiType, @Param("usageDate") usageDate: LocalDate): Int

    @Query("SELECT a.count FROM ApiUsage a WHERE a.apiType = :apiType AND a.usageDate = :usageDate")
    fun findUsageCountByDate(@Param("apiType") apiType: ApiType, @Param("usageDate") usageDate: LocalDate): Optional<Int>
}
```

**Step 3: ErrorCode에 API_USAGE_LIMIT_EXCEEDED 추가 및 기존 코드 유지**

`ErrorCode.kt` line 75 근처에 추가:

```kotlin
// API Usage
API_USAGE_LIMIT_EXCEEDED(FORBIDDEN, "오늘 %s API 사용 한도를 초과했습니다. Date : %s"),
```

기존 `ODSAY_USAGE_LIMIT_EXCEEDED`는 그대로 유지 (하위 호환).

**Step 4: MaxOdsayUsageLimitExceededException → MaxApiUsageLimitExceededException**

기존 파일을 `MaxApiUsageLimitExceededException.kt`로 이름 변경:

```kotlin
package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.ErrorCode.API_USAGE_LIMIT_EXCEEDED
import com.dh.ondot.core.exception.ForbiddenException
import java.time.LocalDate

class MaxApiUsageLimitExceededException(apiTypeName: String, usageDate: LocalDate) :
    ForbiddenException(API_USAGE_LIMIT_EXCEEDED.message.format(apiTypeName, usageDate)) {
    override val errorCode: String get() = API_USAGE_LIMIT_EXCEEDED.name
}
```

**Step 5: OdsayUsageService → ApiUsageService**

```kotlin
package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.core.exception.MaxApiUsageLimitExceededException
import com.dh.ondot.schedule.domain.ApiUsage
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.repository.ApiUsageRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApiUsageService(
    private val apiUsageRepository: ApiUsageRepository,
) {
    @Transactional
    fun checkAndIncrementUsage(apiType: ApiType) {
        val today = TimeUtils.nowSeoulDate()
        val updatedRows = apiUsageRepository.incrementUsageCount(apiType, today)

        if (updatedRows == 0) {
            try {
                apiUsageRepository.save(ApiUsage.newForToday(apiType, today))
            } catch (e: DataIntegrityViolationException) {
                val retried = apiUsageRepository.incrementUsageCount(apiType, today)
                if (retried == 0) {
                    throw MaxApiUsageLimitExceededException(apiType.name, today)
                }
            }
        }
    }

    fun getRemainingUsageToday(apiType: ApiType): Int {
        val today = TimeUtils.nowSeoulDate()
        return apiUsageRepository.findUsageCountByDate(apiType, today)
            .map { count -> maxOf(0, ApiUsage.DAILY_LIMIT - count) }
            .orElse(ApiUsage.DAILY_LIMIT)
    }

    fun getUsageCount(apiType: ApiType, date: java.time.LocalDate): Int =
        apiUsageRepository.findUsageCountByDate(apiType, date).orElse(0)
}
```

**Step 6: 기존 OdsayUsage 파일 삭제, 기존 OdsayUsageRepository 삭제, 기존 OdsayUsageService 삭제, 기존 MaxOdsayUsageLimitExceededException 삭제**

기존 4개 파일을 git rm 하고 새 파일로 대체.

**Step 7: 테스트 업데이트 — OdsayUsageServiceTest → ApiUsageServiceTest**

`OdsayUsageServiceTest.kt`를 `ApiUsageServiceTest.kt`로 이름 변경하고, `OdsayUsageService` → `ApiUsageService`, `OdsayUsageRepository` → `ApiUsageRepository`, `OdsayUsage` → `ApiUsage`, `MaxOdsayUsageLimitExceededException` → `MaxApiUsageLimitExceededException` 전부 치환. `checkAndIncrementUsage()` 호출에 `ApiType.ODSAY` 파라미터 추가.

**Step 8: 빌드 및 테스트 실행**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL (아직 RouteService 등이 참조하므로 컴파일 에러 가능 → 다음 태스크에서 수정)

**Step 9: 커밋**

```
refactor: OdsayUsage를 ApiUsage로 일반화하여 다중 API 사용량 추적 지원
```

---

## Task 3: RouteTimeCalculator 인터페이스 + OdsayRouteTimeCalculator

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/service/RouteTimeCalculator.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/service/OdsayRouteTimeCalculator.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/service/RouteService.kt`

**Step 1: RouteTimeCalculator 인터페이스 생성**

```kotlin
package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.enums.TransportType

interface RouteTimeCalculator {
    fun supports(transportType: TransportType): Boolean
    fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int
}
```

**Step 2: OdsayRouteTimeCalculator 생성**

기존 `RouteService`의 ODSAY 관련 로직을 전부 이동:

```kotlin
package com.dh.ondot.schedule.domain.service

import com.dh.ondot.core.util.GeoUtils
import com.dh.ondot.schedule.domain.enums.ApiType
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.infra.api.OdsayPathApi
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse
import com.dh.ondot.schedule.infra.exception.OdsayTooCloseException
import org.springframework.stereotype.Component
import kotlin.math.round

@Component
class OdsayRouteTimeCalculator(
    private val odsayPathApi: OdsayPathApi,
    private val apiUsageService: ApiUsageService,
) : RouteTimeCalculator {

    override fun supports(transportType: TransportType): Boolean =
        transportType == TransportType.PUBLIC_TRANSPORT

    override fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int {
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

    private fun calculateAdjustedTimesForAllPaths(response: OdsayRouteApiResponse): List<Double> =
        response.result!!
            .path!!.stream()
            .map { calculateAdjustedTimeForSinglePath(it) }
            .sorted()
            .limit(TOP_ROUTES_LIMIT.toLong())
            .toList()

    private fun calculateAdjustedTimeForSinglePath(path: OdsayRouteApiResponse.Path): Double {
        val baseTime = path.info.totalTime
        val transferPenalty = calculateTransferPenalty(path)
        val longWalkPenalty = calculateLongWalkPenalty(path)
        return baseTime + transferPenalty + longWalkPenalty
    }

    private fun calculateTransferPenalty(path: OdsayRouteApiResponse.Path): Double {
        val publicTransportLegs = path.subPath.stream()
            .filter { it.trafficType == SUBWAY_TRAFFIC_TYPE || it.trafficType == BUS_TRAFFIC_TYPE }
            .count()
        val transferCount = maxOf(0L, publicTransportLegs - 1)
        return transferCount * TRANSFER_PENALTY_MINUTES
    }

    private fun calculateLongWalkPenalty(path: OdsayRouteApiResponse.Path): Double {
        val longWalkCount = path.subPath.stream()
            .filter { it.trafficType == WALKING_TRAFFIC_TYPE && it.distance > LONG_WALK_DISTANCE_THRESHOLD }
            .count()
        return longWalkCount * LONG_WALK_PENALTY_MINUTES
    }

    private fun calculateAverageOfTopRoutes(adjustedTimes: List<Double>): Double =
        adjustedTimes.stream()
            .mapToDouble { it }
            .average()
            .orElse(0.0)

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
```

**Step 3: RouteService를 Calculator 위임 방식으로 변경**

```kotlin
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
```

**Step 4: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 5: 커밋**

```
refactor: Strategy 패턴 도입하여 RouteService에서 경로 계산 로직 분리
```

---

## Task 4: TMAP API 연동 (Config, Client, Response, Exception)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/core/config/TmapApiConfig.kt`
- Create: `src/main/kotlin/com/dh/ondot/core/config/TmapRestClientConfig.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/api/TmapPathApi.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/dto/TmapRouteApiResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapServerErrorException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapNoResultException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/TmapUnhandledException.kt`
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt`
- Modify: `src/main/resources/application-prod.yaml` (TMAP 설정 추가)

**Step 1: TmapApiConfig 생성**

```kotlin
package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tmap")
data class TmapApiConfig(
    val baseUrl: String,
    val appKey: String,
)
```

**Step 2: TmapRestClientConfig 생성**

```kotlin
package com.dh.ondot.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class TmapRestClientConfig {

    @Bean
    @Qualifier("tmapRestClient")
    fun tmapRestClient(props: TmapApiConfig): RestClient {
        return RestClient.builder()
            .baseUrl(props.baseUrl)
            .defaultHeader("appKey", props.appKey)
            .build()
    }
}
```

**Step 3: TmapRouteApiResponse 생성**

TMAP 응답은 GeoJSON FeatureCollection 형태. 필요한 필드만 매핑:

```kotlin
package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmapRouteApiResponse(
    val type: String?,
    val features: List<Feature>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Feature(
        val type: String?,
        val properties: Properties?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Properties(
        val totalDistance: Int?,
        val totalTime: Int?,
        val totalFare: Int?,
    )

    fun getTotalTimeSeconds(): Int {
        val props = features?.firstOrNull()?.properties
            ?: throw IllegalStateException("TMAP 응답에 경로 정보가 없습니다.")
        return props.totalTime
            ?: throw IllegalStateException("TMAP 응답에 totalTime이 없습니다.")
    }
}
```

**Step 4: ErrorCode에 TMAP 에러 코드 추가**

`ErrorCode.kt` 마지막 `;` 앞에 추가:

```kotlin
// TMAP API
TMAP_NO_RESULT(NOT_FOUND, "자가용 경로 검색 결과가 없습니다: %s"),
TMAP_SERVER_ERROR(BAD_GATEWAY, "TMAP 서버 내부 오류가 발생했습니다: %s"),
TMAP_UNHANDLED_ERROR(INTERNAL_SERVER_ERROR, "TMAP API 처리 중 알 수 없는 오류가 발생했습니다: %s"),
```

**Step 5: TMAP 예외 클래스 생성**

`TmapServerErrorException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.TMAP_SERVER_ERROR

class TmapServerErrorException(detail: String) :
    BadGatewayException(TMAP_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_SERVER_ERROR.name
}
```

`TmapNoResultException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.TMAP_NO_RESULT

class TmapNoResultException(detail: String) :
    NotFoundException(TMAP_NO_RESULT.message.format(detail)) {
    override val errorCode: String get() = TMAP_NO_RESULT.name
}
```

`TmapUnhandledException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.InternalServerException
import com.dh.ondot.core.exception.ErrorCode.TMAP_UNHANDLED_ERROR

class TmapUnhandledException(detail: String) :
    InternalServerException(TMAP_UNHANDLED_ERROR.message.format(detail)) {
    override val errorCode: String get() = TMAP_UNHANDLED_ERROR.name
}
```

**Step 6: TmapPathApi 생성**

```kotlin
package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.infra.dto.TmapRouteApiResponse
import com.dh.ondot.schedule.infra.exception.TmapNoResultException
import com.dh.ondot.schedule.infra.exception.TmapServerErrorException
import com.dh.ondot.schedule.infra.exception.TmapUnhandledException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TmapPathApi(
    @Qualifier("tmapRestClient") private val tmapRestClient: RestClient,
) {
    @Retryable(
        retryFor = [TmapServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun searchCarRoute(startX: Double, startY: Double, endX: Double, endY: Double): TmapRouteApiResponse {
        try {
            val response = tmapRestClient.post()
                .uri("/tmap/routes?version=1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf(
                    "startX" to startX.toString(),
                    "startY" to startY.toString(),
                    "endX" to endX.toString(),
                    "endY" to endY.toString(),
                    "reqCoordType" to "WGS84GEO",
                    "resCoordType" to "WGS84GEO",
                ))
                .retrieve()
                .body(TmapRouteApiResponse::class.java)

            if (response == null || response.features.isNullOrEmpty()) {
                throw TmapNoResultException("출발지($startX,$startY) → 도착지($endX,$endY)")
            }

            return response
        } catch (ex: TmapServerErrorException) {
            throw ex
        } catch (ex: TmapNoResultException) {
            throw ex
        } catch (e: Exception) {
            throw TmapUnhandledException(e.message ?: "")
        }
    }
}
```

**Step 7: application-prod.yaml에 TMAP 설정 추가**

`odsay:` 설정 아래(line 77 이후)에 추가:

```yaml
tmap:
  base-url: https://apis.openapi.sk.com
  app-key: ${TMAP_APP_KEY}
```

**Step 8: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 9: 커밋**

```
feat: TMAP API 연동 (Config, Client, Response, Exception)
```

---

## Task 5: TmapRouteTimeCalculator 구현

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/service/TmapRouteTimeCalculator.kt`

**Step 1: TmapRouteTimeCalculator 생성**

```kotlin
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
```

**Step 2: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```
feat: TmapRouteTimeCalculator 구현 (자가용 경로 시간 계산)
```

---

## Task 6: API 레이어 변경 (Request/Response/Controller/Facade/Command)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/EstimateTimeRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/SetAlarmRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/ScheduleCreateRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/EstimateTimeResponse.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/ScheduleController.kt` (line 84-91)
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/AlarmController.kt` (line 27-41)
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleQueryFacade.kt` (line 61-66)
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/AlarmFacade.kt` (line 20-31)
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/command/GenerateAlarmCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/command/CreateScheduleCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleCommandFacade.kt` (line 32-84)

**Step 1: EstimateTimeRequest에 transportType 추가**

기존 필드 뒤에 추가:

```kotlin
val transportType: TransportType? = null,
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 2: SetAlarmRequest에 transportType 추가**

기존 필드 뒤에 추가:

```kotlin
val transportType: TransportType? = null,
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 3: EstimateTimeResponse의 Int? → Int 변경**

```kotlin
data class EstimateTimeResponse(
    val estimatedTime: Int,
) {
    companion object {
        @JvmStatic
        fun from(estimatedTime: Int): EstimateTimeResponse {
            return EstimateTimeResponse(estimatedTime)
        }
    }
}
```

**Step 4: GenerateAlarmCommand에 transportType 추가**

```kotlin
data class GenerateAlarmCommand(
    val appointmentAt: LocalDateTime,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
    val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
)
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 5: CreateScheduleCommand에 transportType 추가**

`preparationNote` 아래에 추가:

```kotlin
val transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 6: ScheduleCreateRequest에 transportType 추가 및 toCommand() 수정**

필드 추가 (preparationNote 아래):

```kotlin
val transportType: TransportType? = null,
```

`toCommand()` 메서드에 추가:

```kotlin
transportType = transportType ?: TransportType.PUBLIC_TRANSPORT,
```

**Step 7: ScheduleController.estimateTravelTime 수정** (line 84-91)

```kotlin
override fun estimateTravelTime(
    @Valid @RequestBody request: EstimateTimeRequest,
): EstimateTimeResponse {
    val estimatedTime = scheduleQueryFacade.estimateTravelTime(
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
        request.transportType ?: TransportType.PUBLIC_TRANSPORT,
    )
    return EstimateTimeResponse.from(estimatedTime)
}
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 8: ScheduleQueryFacade.estimateTravelTime 수정** (line 61-66)

```kotlin
fun estimateTravelTime(
    startLongitude: Double, startLatitude: Double,
    endLongitude: Double, endLatitude: Double,
    transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
): Int {
    return routeService.calculateRouteTime(startLongitude, startLatitude, endLongitude, endLatitude, transportType)
}
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 9: AlarmController.setAlarm 수정** (line 27-41)

`GenerateAlarmCommand` 생성 시 `transportType` 전달:

```kotlin
val command = GenerateAlarmCommand(
    request.appointmentAt,
    request.startLongitude, request.startLatitude,
    request.endLongitude, request.endLatitude,
    request.transportType ?: TransportType.PUBLIC_TRANSPORT,
)
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 10: AlarmFacade.generateAlarmSettingByRoute 수정** (line 20-31)

`routeService.calculateRouteTime`에 `transportType` 전달:

```kotlin
val estimatedTimeMin = routeService.calculateRouteTime(
    command.startLongitude, command.startLatitude,
    command.endLongitude, command.endLatitude,
    command.transportType,
)
```

**Step 11: ScheduleCommandFacade.createSchedule 수정** (line 70-84)

`Schedule.createSchedule` 호출에 `transportType` 전달. 이를 위해 `Schedule.createSchedule` 팩토리 메서드도 수정 필요 → Task 7에서 처리.

**Step 12: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: 컴파일 에러 (Schedule.createSchedule 시그니처 변경 필요 → Task 7)

**Step 13: 커밋 (Task 7과 합쳐서)**

---

## Task 7: Schedule 엔티티에 transportType 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/Schedule.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleCommandFacade.kt` (createSchedule)
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/ScheduleDetailResponse.kt` (transportType 포함 여부 확인)

**Step 1: Schedule 엔티티에 transportType 필드 추가**

`preparationNote` 필드 뒤(line 62)에 추가:

```kotlin
@Enumerated(EnumType.STRING)
@Column(name = "transport_type", nullable = false)
var transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
```

import 추가: `import com.dh.ondot.schedule.domain.enums.TransportType`

**Step 2: Schedule.createSchedule 팩토리 메서드에 transportType 파라미터 추가**

```kotlin
fun createSchedule(
    memberId: Long, departurePlace: Place, arrivalPlace: Place,
    preparationAlarm: Alarm, departureAlarm: Alarm, title: String,
    isRepeat: Boolean, repeatDays: SortedSet<Int>?, appointmentAt: LocalDateTime,
    isMedicationRequired: Boolean, preparationNote: String?,
    transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
): Schedule = Schedule(
    memberId = memberId,
    departurePlace = departurePlace,
    arrivalPlace = arrivalPlace,
    preparationAlarm = preparationAlarm,
    departureAlarm = departureAlarm,
    title = title,
    isRepeat = isRepeat,
    repeatDays = if (isRepeat) repeatDays else null,
    appointmentAt = TimeUtils.toInstant(appointmentAt),
    isMedicationRequired = isMedicationRequired,
    preparationNote = preparationNote,
    transportType = transportType,
)
```

**Step 3: ScheduleCommandFacade.createSchedule에서 transportType 전달**

`Schedule.createSchedule()` 호출부(line 70-82)에 `command.transportType` 추가:

```kotlin
val schedule = Schedule.createSchedule(
    memberId,
    departurePlace, arrivalPlace,
    preparationAlarm, departureAlarm,
    command.title, command.isRepeat, TreeSet(command.repeatDays),
    command.appointmentAt, command.isMedicationRequired,
    command.preparationNote, command.transportType,
)
```

**Step 4: Swagger 인터페이스 업데이트**

Swagger 인터페이스(ScheduleSwagger, AlarmSwagger)도 시그니처가 변경되었으므로 확인 후 맞춰 수정.

**Step 5: 빌드 및 전체 테스트 실행**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: 커밋 (Task 6 + Task 7 합산)**

```
feat: API 레이어에 transportType 파라미터 추가 및 Schedule 엔티티 확장
```

---

## Task 8: 전체 통합 확인 및 정리

**Step 1: 전체 빌드 및 테스트**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 2: 기존 ODSAY 흐름 동작 확인**

`transportType` 없이 기존 API 호출 시 기존 ODSAY 로직으로 동작하는지 코드 레벨에서 확인.

**Step 3: 불필요한 import, 미사용 파일 정리**

삭제된 클래스를 참조하는 곳이 없는지 확인.

**Step 4: 커밋**

```
chore: 불필요한 import 및 미사용 코드 정리
```
