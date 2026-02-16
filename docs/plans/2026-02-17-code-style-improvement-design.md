# 코드 스타일 개선 디자인 (baro 기준)

## 목표

baro 프로젝트의 아키텍처 패턴을 참고하여 ondot 코드 품질을 개선한다.

## 발견된 문제와 해결 방안

### A. AuthController.logout() — try-catch 제거

**현재:**
```kotlin
// AuthController.kt
override fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String) {
    try {
        val refreshToken = TokenExtractor.extract(token)
        tokenFacade.logout(refreshToken)
    } catch (_: TokenMissingException) {}
    catch (_: InvalidTokenHeaderException) {}
}
```

**문제:** 컨트롤러에서 try-catch 사용. `TokenFacade.logout()`이 이미 내부에서 모든 예외를 catch하고 있음.

**해결:** 토큰 추출 로직을 `TokenFacade.logout(rawHeader: String)`으로 이동. 컨트롤러는 header 값만 전달.

```kotlin
// AuthController.kt — 개선 후
override fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String) {
    tokenFacade.logoutByHeader(token)
}

// TokenFacade.kt — 신규 메서드
fun logoutByHeader(authorizationHeader: String) {
    try {
        val refreshToken = TokenExtractor.extract(authorizationHeader)
        logout(refreshToken)
    } catch (_: TokenMissingException) {}
    catch (_: InvalidTokenHeaderException) {}
}
```

---

### B. Facade 추상화 레벨 개선

**문제 1: Facade가 Request DTO(presentation)를 직접 받음**

현재 application 레이어가 presentation 레이어에 의존:
- `MemberFacade.onboarding(memberId, mobileType, request: OnboardingRequest)` — presentation DTO 직접 수신
- `ScheduleCommandFacade.createSchedule(memberId, request: ScheduleCreateRequest)` — 30줄+ low-level 필드 접근
- `ScheduleCommandFacade.updateSchedule(memberId, scheduleId, request: ScheduleUpdateRequest)` — 50줄+ low-level 필드 접근
- `ScheduleCommandFacade.createQuickSchedule(memberId, request: QuickScheduleCreateRequest)`

**baro 패턴:** Request DTO에 `toCommand()` 메서드 → facade는 Command만 받음.

**해결:**
1. 기존 Command 클래스들이 이미 OnboardingRequest를 import → Command가 자체적으로 데이터를 들고 있도록 변경
2. ScheduleCreateRequest, ScheduleUpdateRequest, QuickScheduleCreateRequest에 `toCommand()` 추가
3. Facade는 Command만 받음

**적용 대상:**
- `ScheduleCreateRequest.toCommand()` → `CreateScheduleCommand`
- `ScheduleUpdateRequest.toCommand()` → `UpdateScheduleCommand`
- `QuickScheduleCreateRequest.toCommand()` → `CreateQuickScheduleCommand`
- `OnboardingRequest` → 이미 `OnboardingCommand`, `CreateAddressCommand`, `CreateChoicesCommand`가 존재하나 `OnboardingRequest`를 직접 import. `toCommand()` 패턴으로 전환.

**문제 2: Facade가 원시 파라미터를 너무 많이 받음**

- `MemberFacade.deleteMember(memberId, withdrawalReasonId, customReason)` — 3개 파라미터
- `MemberFacade.updateHomeAddress(memberId, roadAddress, longitude, latitude)` — 4개 파라미터
- `PlaceFacade.saveHistory(memberId, title, roadAddr, longitude, latitude)` — 5개 파라미터
- `AlarmFacade.generateAlarmSettingByRoute(memberId, appointmentAt, startX, startY, endX, endY)` — 6개 파라미터

**해결:** 3개 이상의 파라미터는 Command로 그룹핑.

---

### C. Facade → Presentation 의존 역전 해소

**현재 application→presentation 의존 목록:**
```
AuthFacade → LoginResponse (presentation.response)
MemberFacade → OnboardingRequest, OnboardingResponse (presentation)
ScheduleCommandFacade → ScheduleCreateRequest, ScheduleUpdateRequest, QuickScheduleCreateRequest, ScheduleParsedResponse
ScheduleQueryFacade → presentation.response.* (HomeScheduleListResponse 등)
Command 클래스들 → OnboardingRequest
HomeScheduleListItem → AlarmDto (presentation.response)
OpenAiPromptApi → ScheduleParsedResponse (presentation.response)
QuickScheduleMapper → QuickScheduleCreateRequest, PlaceDto
```

**baro 패턴:** Facade는 도메인 객체 또는 application DTO(Bundle)를 반환. 컨트롤러가 Response로 변환.

**해결 (영향 범위를 고려한 단계적 접근):**

**Phase 1 — 즉시 개선 (risk 낮음):**
- `AuthFacade.loginWithOAuth()` → `LoginResult` (application DTO) 반환, 컨트롤러가 `LoginResponse`로 변환
- `MemberFacade.onboarding()` → `OnboardingResult` (application DTO) 반환
- Command 클래스들의 `OnboardingRequest` import → Command 자체에 데이터를 받도록 변경
- `ScheduleCommandFacade.parseVoiceSchedule()` → application DTO 반환

**Phase 2 — 후속 개선 (영향 범위 큼):**
- `ScheduleQueryFacade`가 `HomeScheduleListResponse`를 반환하는 패턴 → application DTO로 분리
- `HomeScheduleListItem`의 `AlarmDto` 의존 → application 레이어 DTO로 분리
- `OpenAiPromptApi`의 `ScheduleParsedResponse` → 인프라 자체 DTO로 분리

---

### D. Controller → Domain Service 직접 의존 제거

**현재:**
```kotlin
// ScheduleController.kt
class ScheduleController(
    ...
    private val routeService: RouteService,  // 도메인 서비스 직접 의존
) {
    fun estimateTravelTime(request: EstimateTimeRequest): EstimateTimeResponse {
        val estimatedTime = routeService.calculateRouteTime(...)  // 직접 호출
        return EstimateTimeResponse.from(estimatedTime)
    }
}

// AlarmController.kt
class AlarmController(
    ...
    private val alarmService: AlarmService,  // 도메인 서비스 직접 의존
) {
    fun recordAlarmTrigger(...) {
        alarmService.recordTrigger(...)  // 직접 호출
    }
}
```

**baro 패턴:** 컨트롤러는 항상 facade만 의존. 도메인 서비스 직접 접근 금지.

**해결:**
- `ScheduleController.estimateTravelTime()` → `ScheduleQueryFacade.estimateTravelTime()` 경유
- `AlarmController.recordAlarmTrigger()` → `AlarmFacade.recordAlarmTrigger()` 경유
- 컨트롤러에서 `RouteService`, `AlarmService` 의존 제거

---

### E. GlobalExceptionHandler — MissingRequestHeaderException 핸들러 추가

**현재:** `@RequestHeader`에 필수 헤더 누락 시 `MissingRequestHeaderException` → catch-all 핸들러 → 500

**해결:** 전용 핸들러 추가하여 400 반환

```kotlin
@ExceptionHandler(MissingRequestHeaderException::class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
fun handleMissingRequestHeader(e: MissingRequestHeaderException): ErrorResponse {
    log.warn(e.message)
    return ErrorResponse(ErrorCode.MISSING_REQUEST_HEADER)
}
```

---

### F. ErrorCode 추가

`MISSING_REQUEST_HEADER` ErrorCode 추가.

---

## 영향 범위 요약

| 개선 항목 | 변경 파일 수 | 위험도 |
|-----------|------------|--------|
| A. AuthController try-catch | 2 (AuthController, TokenFacade) | 낮음 |
| B. Request → Command 변환 | ~10 (Request DTO, Command, Facade) | 중간 |
| C. Facade→Presentation 의존 해소 (Phase 1) | ~8 (Facade, Controller, application DTO) | 중간 |
| D. Controller→Service 직접 의존 제거 | 4 (2 Controller, 2 Facade) | 낮음 |
| E. ExceptionHandler 보강 | 2 (GlobalExceptionHandler, ErrorCode) | 낮음 |
| F. ErrorCode 추가 | 1 | 낮음 |

## 진행 순서

1. E+F (예외 핸들러) — 독립적, 가장 안전
2. A (try-catch 제거) — 독립적, 안전
3. D (Controller→Service 직접 의존 제거) — 소규모 변경
4. B+C (Facade 추상화 + 의존 방향) — 가장 큰 변경, 마지막에 진행
