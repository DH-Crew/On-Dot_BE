# DH-23: 자가용 기능 구현 설계

## 개요

기존 ODSAY API (대중교통 전용) 외에 TMAP API를 활용한 자가용 경로 시간 계산 기능을 추가한다.
기존 API에 선택적 파라미터 `transportType`을 추가하여 하위 호환성을 유지한다.

## 핵심 결정 사항

| 항목 | 결정 |
|------|------|
| 자가용 API | TMAP 자동차 경로 탐색 API (일 1,000건 무료) |
| 파라미터 | `transportType: TransportType?` (null이면 PUBLIC_TRANSPORT) |
| 시간 보정 | TMAP 결과 + 10분 고정 버퍼 (패널티/% 여유 없음) |
| 실패 처리 | 2회 재시도 후 예외 (fallback 없음) |
| 사용량 추적 | `OdsayUsage` → `ApiUsage`로 일반화, `apiType` 컬럼으로 구분 |
| 아키텍처 | Strategy 패턴으로 경로 계산 로직 분리 |
| 스케줄 저장 | Schedule 엔티티에 `transportType` 컬럼 추가 |

## 아키텍처: Strategy 패턴

```
Controller (transportType 수신)
  → Facade (전달)
    → RouteService (Calculator 선택)
      → RouteTimeCalculator 인터페이스
        ├─ OdsayRouteTimeCalculator (대중교통)
        │    └─ OdsayPathApi → ODSAY API
        └─ TmapRouteTimeCalculator (자가용)
             └─ TmapPathApi → TMAP API
```

### RouteTimeCalculator 인터페이스

```kotlin
interface RouteTimeCalculator {
    fun supports(transportType: TransportType): Boolean
    fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int
}
```

### RouteService 변경

기존 계산 로직을 제거하고 Calculator에 위임:

```kotlin
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

## Enum 정의

### TransportType

```kotlin
enum class TransportType {
    PUBLIC_TRANSPORT,
    CAR
}
```

### ApiType

```kotlin
enum class ApiType {
    ODSAY, TMAP
}
```

## API 변경

### transportType 파라미터 추가 대상

| API | Request DTO | 용도 |
|-----|-------------|------|
| `POST /schedules/estimate-time` | `EstimateTimeRequest` | 예상시간 조회 |
| `POST /alarms/setting` | `SetAlarmRequest` | 알람 설정 시 경로 계산 |
| `POST /schedules` | `ScheduleCreateRequest` | 스케줄에 교통수단 저장 |

모든 파라미터는 선택값이며, null일 경우 `PUBLIC_TRANSPORT`로 기본 처리.

### EstimateTimeResponse 수정

`estimatedTime: Int?` → `estimatedTime: Int` (nullable 제거)

## TMAP API 연동

### 설정

```kotlin
@ConfigurationProperties(prefix = "tmap")
data class TmapApiConfig(
    val baseUrl: String,  // https://apis.openapi.sk.com
    val appKey: String,
)
```

### TmapPathApi

- TMAP 자동차 경로 탐색 API (`/tmap/routes`) 호출
- `@Retryable` 2회 재시도, 500ms backoff
- 응답의 `totalTime` (초 단위) → 분 단위 변환
- 에러 코드별 커스텀 예외 매핑

### TmapRouteTimeCalculator

- TmapPathApi 호출
- 결과 시간(분) + 10분 고정 버퍼
- 패널티 계산 없음

## ApiUsage 통합

### 엔티티 변경

`OdsayUsage` → `ApiUsage`:

```kotlin
@Entity
class ApiUsage(
    val apiType: ApiType,
    val usageDate: LocalDate,
    var count: Int = 0,
)
```

### 서비스 변경

`OdsayUsageService` → `ApiUsageService`:
- `checkAndIncrementUsage(apiType: ApiType)`
- 일 제한: ODSAY 1,000건, TMAP 1,000건

## Schedule 엔티티 변경

```kotlin
@Enumerated(EnumType.STRING)
@Column(nullable = false)
var transportType: TransportType = TransportType.PUBLIC_TRANSPORT
```

기본값 `PUBLIC_TRANSPORT` → 기존 데이터 호환.

## 변경 영향 범위

### 신규 파일

- `TransportType` enum
- `ApiType` enum
- `RouteTimeCalculator` 인터페이스
- `OdsayRouteTimeCalculator` 구현체
- `TmapRouteTimeCalculator` 구현체
- `TmapApiConfig`, `TmapRestClientConfig`
- `TmapPathApi`, `TmapRouteApiResponse`
- TMAP 전용 예외 클래스

### 수정 파일

- `EstimateTimeRequest` — transportType 추가
- `SetAlarmRequest` — transportType 추가
- `ScheduleCreateRequest` — transportType 추가
- `EstimateTimeResponse` — Int? → Int
- `RouteService` — Calculator 위임으로 변경
- `ScheduleQueryFacade` — transportType 전달
- `AlarmFacade` — transportType 전달
- `Schedule` 엔티티 — transportType 컬럼 추가
- `OdsayUsage` → `ApiUsage` 리네이밍
- `OdsayUsageService` → `ApiUsageService` 리네이밍
- `OdsayUsageRepository` → `ApiUsageRepository` 리네이밍
- `application.yml` — TMAP 설정 추가

## 제외 사항

- 빠른 일정 생성(`POST /schedules/quick`) — 현재 미사용으로 제외
- TMAP 실패 시 ODSAY fallback — 혼란 방지를 위해 미적용
