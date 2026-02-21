# 에브리타임(에타) 연동 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 에브리타임 시간표 공유 URL을 기반으로 요일별 첫 수업 시작시간에 맞춰 반복 스케줄과 알람을 자동 생성한다.

**Architecture:** Infra 레이어에 EverytimeApi(RestClient + XML 파싱)를 추가하고, 기존 ScheduleCommandFacade에 검증/생성 메서드를 추가한다. 경로 계산은 기존 RouteService를 재사용한다.

**Tech Stack:** Spring Boot 3, Kotlin, RestClient, XML(Jackson XmlMapper), JPA

---

### Task 1: 에브리타임 예외 클래스 + ErrorCode 추가

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/EverytimeInvalidUrlException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/EverytimeNotFoundException.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/exception/EverytimeServerException.kt`
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt:86` (enum 끝 부분에 추가)

**Step 1: ErrorCode에 에브리타임 관련 코드 추가**

`ErrorCode.kt`의 `TMAP_UNHANDLED_ERROR` 뒤에 추가:

```kotlin
// Everytime API
EVERYTIME_INVALID_URL(BAD_REQUEST, "에브리타임 URL 형식이 올바르지 않습니다: %s"),
EVERYTIME_NOT_FOUND(NOT_FOUND, "에브리타임 시간표를 찾을 수 없습니다. 공유 URL을 확인해주세요."),
EVERYTIME_EMPTY_TIMETABLE(NOT_FOUND, "시간표에 등록된 수업이 없습니다."),
EVERYTIME_SERVER_ERROR(BAD_GATEWAY, "에브리타임 서버에 일시적인 오류가 발생했습니다: %s"),
```

**Step 2: 예외 클래스 3개 생성**

`EverytimeInvalidUrlException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_INVALID_URL

class EverytimeInvalidUrlException(detail: String) :
    BadRequestException(EVERYTIME_INVALID_URL.message.format(detail)) {
    override val errorCode: String get() = EVERYTIME_INVALID_URL.name
}
```

`EverytimeNotFoundException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_NOT_FOUND

class EverytimeNotFoundException :
    NotFoundException(EVERYTIME_NOT_FOUND.message) {
    override val errorCode: String get() = EVERYTIME_NOT_FOUND.name
}
```

`EverytimeServerException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.BadGatewayException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_SERVER_ERROR

class EverytimeServerException(detail: String) :
    BadGatewayException(EVERYTIME_SERVER_ERROR.message.format(detail)) {
    override val errorCode: String get() = EVERYTIME_SERVER_ERROR.name
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```
feat: 에브리타임 예외 클래스 및 ErrorCode 추가
```

---

### Task 2: EverytimeRestClientConfig + EverytimeApi (Infra 레이어)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/core/config/EverytimeRestClientConfig.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/api/EverytimeApi.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/dto/EverytimeLecture.kt`

**Step 1: RestClient 설정**

`EverytimeRestClientConfig.kt`:
```kotlin
package com.dh.ondot.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class EverytimeRestClientConfig {

    @Bean
    @Qualifier("everytimeRestClient")
    fun everytimeRestClient(): RestClient {
        val settings = ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(Duration.ofSeconds(3))
            .withReadTimeout(Duration.ofSeconds(5))
        val factory = ClientHttpRequestFactoryBuilder.detect().build(settings)

        return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("https://api.everytime.kr")
            .defaultHeader("Origin", "https://everytime.kr")
            .defaultHeader("Referer", "https://everytime.kr/")
            .build()
    }
}
```

**Step 2: DTO 생성**

`EverytimeLecture.kt`:
```kotlin
package com.dh.ondot.schedule.application.dto

import java.time.LocalTime

data class EverytimeLecture(
    val name: String,
    val day: Int,          // 에브리타임 기준: 0=월 ~ 6=일
    val startTime: LocalTime,
    val endTime: LocalTime,
    val place: String,
)
```

**Step 3: EverytimeApi 구현**

`EverytimeApi.kt` — RestClient로 에브리타임 API 호출 + XML 파싱:
```kotlin
package com.dh.ondot.schedule.infra.api

import com.dh.ondot.schedule.application.dto.EverytimeLecture
import com.dh.ondot.schedule.infra.exception.EverytimeNotFoundException
import com.dh.ondot.schedule.infra.exception.EverytimeServerException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.w3c.dom.Element
import java.time.LocalTime
import javax.xml.parsers.DocumentBuilderFactory

@Component
class EverytimeApi(
    @Qualifier("everytimeRestClient") private val everytimeRestClient: RestClient,
) {
    @Retryable(
        retryFor = [EverytimeServerException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500),
    )
    fun fetchTimetable(identifier: String): List<EverytimeLecture> {
        val xml = callApi(identifier)
        return parseXml(xml)
    }

    private fun callApi(identifier: String): String {
        try {
            val response = everytimeRestClient.post()
                .uri("/find/timetable/table/friend")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("identifier=$identifier&friendInfo=true")
                .retrieve()
                .body(String::class.java)

            if (response.isNullOrBlank()) {
                throw EverytimeNotFoundException()
            }
            return response
        } catch (ex: EverytimeNotFoundException) {
            throw ex
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.is5xxServerError) {
                throw EverytimeServerException("${ex.statusCode}: ${ex.message}")
            }
            throw EverytimeServerException("${ex.statusCode}: ${ex.message}")
        } catch (ex: Exception) {
            throw EverytimeServerException("${ex.javaClass.simpleName}: ${ex.message}")
        }
    }

    private fun parseXml(xml: String): List<EverytimeLecture> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xml.byteInputStream())
        val subjects = document.getElementsByTagName("subject")

        if (subjects.length == 0) {
            throw EverytimeNotFoundException()
        }

        val lectures = mutableListOf<EverytimeLecture>()

        for (i in 0 until subjects.length) {
            val subject = subjects.item(i) as Element
            val name = subject.getElementsByTagName("name").item(0)
                ?.let { (it as Element).getAttribute("value") } ?: continue

            val timeElements = subject.getElementsByTagName("data")
            for (j in 0 until timeElements.length) {
                val data = timeElements.item(j) as Element
                val day = data.getAttribute("day").toIntOrNull() ?: continue
                val startSlot = data.getAttribute("starttime").toIntOrNull() ?: continue
                val endSlot = data.getAttribute("endtime").toIntOrNull() ?: continue
                val place = data.getAttribute("place") ?: ""

                lectures.add(
                    EverytimeLecture(
                        name = name,
                        day = day,
                        startTime = slotToLocalTime(startSlot),
                        endTime = slotToLocalTime(endSlot),
                        place = place,
                    )
                )
            }
        }

        return lectures
    }

    private fun slotToLocalTime(slot: Int): LocalTime {
        val totalMinutes = slot * 5
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
    }
}
```

**Step 4: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 5: 커밋**

```
feat: 에브리타임 RestClient 및 API 클라이언트 구현
```

---

### Task 3: Request/Response DTO 추가

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/EverytimeValidateRequest.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/EverytimeScheduleCreateRequest.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/EverytimeValidateResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/EverytimeScheduleCreateResponse.kt`

**Step 1: Request DTO 생성**

`EverytimeValidateRequest.kt`:
```kotlin
package com.dh.ondot.schedule.presentation.request

import jakarta.validation.constraints.NotBlank

data class EverytimeValidateRequest(
    @field:NotBlank(message = "everytimeUrl은 필수입니다.")
    val everytimeUrl: String,
)
```

`EverytimeScheduleCreateRequest.kt` (SetAlarmRequest 네이밍 패턴):
```kotlin
package com.dh.ondot.schedule.presentation.request

import com.dh.ondot.schedule.domain.enums.TransportType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EverytimeScheduleCreateRequest(
    @field:NotBlank(message = "everytimeUrl은 필수입니다.")
    val everytimeUrl: String,

    @field:NotNull(message = "startLongitude는 필수입니다.")
    @field:DecimalMin(value = "-180.0", message = "startLongitude는 -180 이상이어야 합니다.")
    @field:DecimalMax(value = "180.0", message = "startLongitude는 180 이하이어야 합니다.")
    val startLongitude: Double,

    @field:NotNull(message = "startLatitude는 필수입니다.")
    @field:DecimalMin(value = "-90.0", message = "startLatitude는 -90 이상이어야 합니다.")
    @field:DecimalMax(value = "90.0", message = "startLatitude는 90 이하이어야 합니다.")
    val startLatitude: Double,

    @field:NotNull(message = "endLongitude는 필수입니다.")
    @field:DecimalMin(value = "-180.0", message = "endLongitude는 -180 이상이어야 합니다.")
    @field:DecimalMax(value = "180.0", message = "endLongitude는 180 이하이어야 합니다.")
    val endLongitude: Double,

    @field:NotNull(message = "endLatitude는 필수입니다.")
    @field:DecimalMin(value = "-90.0", message = "endLatitude는 -90 이상이어야 합니다.")
    @field:DecimalMax(value = "90.0", message = "endLatitude는 90 이하이어야 합니다.")
    val endLatitude: Double,

    val transportType: TransportType? = null,
)
```

**Step 2: Response DTO 생성**

`EverytimeValidateResponse.kt`:
```kotlin
package com.dh.ondot.schedule.presentation.response

data class EverytimeValidateResponse(
    val identifier: String,
    val isValid: Boolean,
)
```

`EverytimeScheduleCreateResponse.kt`:
```kotlin
package com.dh.ondot.schedule.presentation.response

data class EverytimeScheduleCreateResponse(
    val createdCount: Int,
    val schedules: List<EverytimeScheduleItem>,
) {
    data class EverytimeScheduleItem(
        val scheduleId: Long,
        val title: String,
        val repeatDays: List<Int>,
        val appointmentAt: String,
    )
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```
feat: 에브리타임 연동 Request/Response DTO 추가
```

---

### Task 4: ScheduleCommandFacade에 검증 + 스케줄 생성 로직 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleCommandFacade.kt`

**Step 1: EverytimeApi 의존성 주입 + URL 파싱 유틸 메서드 추가**

생성자에 `EverytimeApi` 추가, `validateEverytimeUrl`과 `createSchedulesFromEverytime` 메서드 추가.

핵심 비즈니스 로직:

```kotlin
import com.dh.ondot.schedule.infra.api.EverytimeApi
import com.dh.ondot.schedule.infra.exception.EverytimeInvalidUrlException
import com.dh.ondot.schedule.application.dto.EverytimeLecture
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.core.util.TimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.net.URI

// 생성자에 추가: private val everytimeApi: EverytimeApi

fun validateEverytimeUrl(url: String): String {
    val identifier = extractIdentifier(url)
    everytimeApi.fetchTimetable(identifier) // 유효성 확인 (예외 발생 시 전파)
    return identifier
}

@Transactional
fun createSchedulesFromEverytime(
    memberId: Long,
    url: String,
    startX: Double, startY: Double,
    endX: Double, endY: Double,
    transportType: TransportType,
): List<Schedule> {
    val member = memberService.getMemberIfExists(memberId)
    val identifier = extractIdentifier(url)
    val lectures = everytimeApi.fetchTimetable(identifier)

    if (lectures.isEmpty()) {
        throw EverytimeEmptyTimetableException()
    }

    // 1. 요일별 첫 수업 시작시간 추출
    val firstLectureByDay: Map<Int, LocalTime> = lectures
        .groupBy { it.day }
        .mapValues { (_, dayLectures) -> dayLectures.minOf { it.startTime } }

    // 2. 동일 시간으로 그룹핑 (월→일 순서)
    val dayOrder = listOf(0, 1, 2, 3, 4, 5, 6) // 월~일
    val timeGroups: Map<LocalTime, List<Int>> = firstLectureByDay.entries
        .groupBy({ it.value }, { it.key })
        .mapValues { (_, days) -> days.sortedBy { dayOrder.indexOf(it) } }

    // 3. 경로 계산
    val routeTimeByGroup = calculateRouteTimeByGroup(
        timeGroups, startX, startY, endX, endY, transportType,
    )

    // 4. 그룹별 Schedule + Alarm 생성
    val schedules = timeGroups.map { (time, days) ->
        val title = buildScheduleTitle(days)
        val repeatDays = days.map { everytimeDayToRepeatDay(it) }.toSortedSet()
        val appointmentAt = calculateNextAppointmentAt(days, time)
        val estimatedTime = routeTimeByGroup[time]!!

        val schedule = scheduleService.setupSchedule(member, appointmentAt, estimatedTime)
        schedule.memberId = member.id
        schedule.title = title
        schedule.isRepeat = true
        schedule.repeatDays = TreeSet(repeatDays)
        schedule.appointmentAt = TimeUtils.toInstant(appointmentAt)
        schedule.transportType = transportType

        scheduleService.saveSchedule(schedule)
    }

    return schedules
}

private fun extractIdentifier(url: String): String {
    try {
        val uri = URI(url)
        if (uri.host != "everytime.kr") {
            throw EverytimeInvalidUrlException(url)
        }
        val path = uri.path
        if (!path.startsWith("/@")) {
            throw EverytimeInvalidUrlException(url)
        }
        val identifier = path.removePrefix("/@")
        if (identifier.isBlank()) {
            throw EverytimeInvalidUrlException(url)
        }
        return identifier
    } catch (e: EverytimeInvalidUrlException) {
        throw e
    } catch (e: Exception) {
        throw EverytimeInvalidUrlException(url)
    }
}

private fun calculateRouteTimeByGroup(
    timeGroups: Map<LocalTime, List<Int>>,
    startX: Double, startY: Double,
    endX: Double, endY: Double,
    transportType: TransportType,
): Map<LocalTime, Int> {
    if (transportType == TransportType.PUBLIC_TRANSPORT) {
        // 대중교통: 1회 조회 후 전체 재사용
        val routeTime = routeService.calculateRouteTime(startX, startY, endX, endY, transportType)
        return timeGroups.keys.associateWith { routeTime }
    }

    // 자가용: 시간대별 조회 (첫 번째 요일 기준)
    return timeGroups.map { (time, days) ->
        val representativeDay = days.first()
        val appointmentAt = calculateNextAppointmentAt(listOf(representativeDay), time)
        val routeTime = routeService.calculateRouteTime(
            startX, startY, endX, endY, transportType, appointmentAt,
        )
        time to routeTime
    }.toMap()
}

private fun buildScheduleTitle(days: List<Int>): String {
    val dayNames = mapOf(
        0 to "월", 1 to "화", 2 to "수", 3 to "목",
        4 to "금", 5 to "토", 6 to "일",
    )
    val dayStr = days.joinToString("/") { dayNames[it]!! }
    return "${dayStr}요일 학교"
}

// 에브리타임 day(0=월~6=일) → DB repeatDays(1=일~7=토)
private fun everytimeDayToRepeatDay(everytimeDay: Int): Int =
    when (everytimeDay) {
        0 -> 2  // 월
        1 -> 3  // 화
        2 -> 4  // 수
        3 -> 5  // 목
        4 -> 6  // 금
        5 -> 7  // 토
        6 -> 1  // 일
        else -> throw IllegalArgumentException("잘못된 에브리타임 요일: $everytimeDay")
    }

// 그룹 내 첫 번째 요일(월→일 순)의 가장 가까운 미래 날짜 + 시작시간
private fun calculateNextAppointmentAt(days: List<Int>, time: LocalTime): LocalDateTime {
    val today = TimeUtils.nowSeoulDate()
    val now = TimeUtils.nowSeoulDateTime()
    val targetDaysOfWeek = days.map { everytimeDayToDayOfWeek(it) }

    for (daysAhead in 0..7L) {
        val candidateDate = today.plusDays(daysAhead)
        if (candidateDate.dayOfWeek in targetDaysOfWeek) {
            val candidateDateTime = candidateDate.atTime(time)
            if (candidateDateTime.isAfter(now)) {
                return candidateDateTime
            }
        }
    }
    // fallback: 다음 주 첫 번째 요일
    return today.plusDays(7).atTime(time)
}

private fun everytimeDayToDayOfWeek(everytimeDay: Int): DayOfWeek =
    when (everytimeDay) {
        0 -> DayOfWeek.MONDAY
        1 -> DayOfWeek.TUESDAY
        2 -> DayOfWeek.WEDNESDAY
        3 -> DayOfWeek.THURSDAY
        4 -> DayOfWeek.FRIDAY
        5 -> DayOfWeek.SATURDAY
        6 -> DayOfWeek.SUNDAY
        else -> throw IllegalArgumentException("잘못된 에브리타임 요일: $everytimeDay")
    }
```

**Step 2: EverytimeEmptyTimetableException 추가**

`src/main/kotlin/com/dh/ondot/schedule/infra/exception/EverytimeEmptyTimetableException.kt`:
```kotlin
package com.dh.ondot.schedule.infra.exception

import com.dh.ondot.core.exception.NotFoundException
import com.dh.ondot.core.exception.ErrorCode.EVERYTIME_EMPTY_TIMETABLE

class EverytimeEmptyTimetableException :
    NotFoundException(EVERYTIME_EMPTY_TIMETABLE.message) {
    override val errorCode: String get() = EVERYTIME_EMPTY_TIMETABLE.name
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```
feat: 에브리타임 URL 검증 및 스케줄 일괄 생성 로직 구현
```

---

### Task 5: ScheduleController에 에타 엔드포인트 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/ScheduleController.kt`

**Step 1: 검증 + 생성 엔드포인트 추가**

`ScheduleController.kt`의 `deleteSchedule` 메서드 뒤에 추가:

```kotlin
@ResponseStatus(HttpStatus.OK)
@PostMapping("/everytime/validate")
fun validateEverytimeUrl(
    @Valid @RequestBody request: EverytimeValidateRequest,
): EverytimeValidateResponse {
    val identifier = scheduleCommandFacade.validateEverytimeUrl(request.everytimeUrl)
    return EverytimeValidateResponse(identifier = identifier, isValid = true)
}

@ResponseStatus(HttpStatus.CREATED)
@PostMapping("/everytime")
fun createSchedulesFromEverytime(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: EverytimeScheduleCreateRequest,
): EverytimeScheduleCreateResponse {
    val transportType = request.transportType ?: TransportType.PUBLIC_TRANSPORT
    val schedules = scheduleCommandFacade.createSchedulesFromEverytime(
        memberId,
        request.everytimeUrl,
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
        transportType,
    )
    return EverytimeScheduleCreateResponse(
        createdCount = schedules.size,
        schedules = schedules.map {
            EverytimeScheduleCreateResponse.EverytimeScheduleItem(
                scheduleId = it.id,
                title = it.title,
                repeatDays = it.repeatDays?.toList() ?: emptyList(),
                appointmentAt = TimeUtils.toSeoulDateTime(it.appointmentAt).toString(),
            )
        },
    )
}
```

import 추가:
```kotlin
import com.dh.ondot.schedule.presentation.request.EverytimeValidateRequest
import com.dh.ondot.schedule.presentation.request.EverytimeScheduleCreateRequest
import com.dh.ondot.schedule.presentation.response.EverytimeValidateResponse
import com.dh.ondot.schedule.presentation.response.EverytimeScheduleCreateResponse
import com.dh.ondot.core.util.TimeUtils
```

**Step 2: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```
feat: 에브리타임 검증 및 스케줄 생성 API 엔드포인트 추가
```

---

### Task 6: Swagger 문서 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/swagger/ScheduleSwagger.kt`

**Step 1: 에타 검증 API Swagger 추가**

`ScheduleSwagger.kt`의 `deleteSchedule` 메서드 뒤에 에브리타임 검증/생성 API의 Swagger 정의를 추가한다. 기존 Swagger 패턴(Operation, RequestBody, ApiResponse)을 따른다.

**Step 2: 에타 스케줄 생성 API Swagger 추가**

요청 예시와 응답 예시를 포함한다.

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```
docs: 에브리타임 API Swagger 문서 추가
```

---

### Task 7: 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/schedule/infra/api/EverytimeApiTest.kt`
- Create: `src/test/kotlin/com/dh/ondot/schedule/application/EverytimeScheduleCreationTest.kt`

**Step 1: EverytimeApi XML 파싱 테스트**

XML 응답 문자열을 직접 작성하여 파싱 로직을 검증한다:
- 정상 시간표 파싱 (다수 과목, 다수 요일)
- 빈 시간표 (subject 없음) → EverytimeNotFoundException
- slot → LocalTime 변환 정확성 (114 → 09:30)

**Step 2: 비즈니스 로직 단위 테스트**

Facade의 비즈니스 로직 (그룹핑, 이름 생성, 요일 매핑)을 검증:
- 동일 시간 그룹핑: 월/화/수 09:30 → 하나의 그룹
- 스케줄 이름 생성: [0,2,4] → "월/수/금요일 학교"
- 요일 매핑: everytimeDay 0 → repeatDay 2
- URL 파싱: `https://everytime.kr/@abc123` → `abc123`
- 잘못된 URL → EverytimeInvalidUrlException

**Step 3: 테스트 실행**

Run: `./gradlew test`
Expected: 모든 테스트 PASS

**Step 4: 커밋**

```
test: 에브리타임 API 파싱 및 스케줄 생성 로직 테스트 추가
```

---

### Task 8: 전체 빌드 + 통합 확인

**Step 1: 전체 빌드**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: 전체 테스트**

Run: `./gradlew test`
Expected: 모든 테스트 PASS

**Step 3: 최종 커밋 (필요 시)**

누락된 변경사항이 있으면 커밋한다.
