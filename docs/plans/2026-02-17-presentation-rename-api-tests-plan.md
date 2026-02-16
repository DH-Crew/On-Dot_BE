# Presentation 패키지 리네이밍 + API 테스트 구현 플랜

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `api` 패키지를 `presentation`으로 rename하고, 전체 27개 엔드포인트에 대한 `@WebMvcTest` 슬라이스 테스트를 작성한다.

**Architecture:** 패키지 rename은 디렉토리 이동 + package/import 일괄 변경으로 수행. API 테스트는 `@WebMvcTest`로 컨트롤러 레이어만 격리하여 테스트하며, Facade/Service는 `@MockBean`으로 모킹. `TokenInterceptor`를 `@MockBean`으로 처리하여 인증 통과/실패 시나리오를 모두 커버.

**Tech Stack:** Kotlin, Spring Boot 3.4.4, MockMvc, JUnit 5, AssertJ, Mockito, Jackson

**JAVA_HOME:** `export JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home`

---

## 인증 패턴 (모든 테스트 공통)

### WebConfig 인터셉터 등록 현황
```
인증 필요: /members/**, /alarms/**, /places/**, /schedules/**
인증 제외: /schedules/*/issues, /schedules/*/preparation
인증 불필요: /auth/** (인터셉터 패턴에 미포함)
```

### 테스트에서 인증 처리
- `TokenInterceptor`를 `@MockBean`으로 등록
- `WebConfig`를 `@MockBean`으로 등록 (인터셉터 등록 비활성화)
- 인증 통과: 테스트 시 `request.setAttribute("memberId", 1L)` 직접 설정 (MockMvc `requestAttr`)
- 인증 실패 테스트: `WebConfig`를 mock하지 않는 별도 테스트 클래스 또는 `TokenInterceptor`가 예외를 throw하도록 설정

### @WebMvcTest에서의 주의사항
- `@WebMvcTest`는 지정한 Controller만 로딩하므로, `WebConfig`도 `@Import`하거나 `@MockBean`으로 처리해야 함
- `OauthProviderConverter`도 `WebConfig`에서 사용하므로 함께 고려

---

## Task 1: member 패키지 rename (`api` → `presentation`)

**Files:**
- Move: `src/main/kotlin/com/dh/ondot/member/api/` → `src/main/kotlin/com/dh/ondot/member/presentation/`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/infra/api/OpenAiPromptApi.kt` (이 파일이 member.api를 import하는지 확인)

**Step 1: 디렉토리 이동**
```bash
mv src/main/kotlin/com/dh/ondot/member/api src/main/kotlin/com/dh/ondot/member/presentation
```

**Step 2: package 선언 일괄 변경**
모든 `src/main/kotlin/com/dh/ondot/member/presentation/` 하위 파일에서:
```
com.dh.ondot.member.api → com.dh.ondot.member.presentation
```
대상 파일 (~16개):
- `AuthController.kt`, `MemberController.kt`
- `request/`: OnboardingRequest, UpdateHomeAddressRequest, UpdateMapProviderRequest, UpdatePreparationTimeRequest, WithdrawalRequest
- `response/`: AccessToken, HomeAddressResponse, LoginResponse, MapProviderResponse, OnboardingResponse, PreparationTimeResponse, UpdateHomeAddressResponse
- `swagger/`: AuthSwagger, MemberSwagger

**Step 3: import 경로 변경**
`member.api`를 import하는 모든 파일에서 `member.api` → `member.presentation` 변경.
주로 presentation 패키지 내부 파일 간 import이므로 Step 2에서 함께 처리됨.

**Step 4: 빌드 검증**
```bash
export JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home && ./gradlew clean build 2>&1 | tail -20
```

**Step 5: 커밋**
```bash
git add -A && git commit -m "refactor: member/api 패키지를 member/presentation으로 rename"
```

---

## Task 2: schedule 패키지 rename (`api` → `presentation`)

**Files:**
- Move: `src/main/kotlin/com/dh/ondot/schedule/api/` → `src/main/kotlin/com/dh/ondot/schedule/presentation/`
- Modify: imports in `schedule/application/`, `schedule/infra/api/OpenAiPromptApi.kt` 등

**Step 1: 디렉토리 이동**
```bash
mv src/main/kotlin/com/dh/ondot/schedule/api src/main/kotlin/com/dh/ondot/schedule/presentation
```

**Step 2: package 선언 일괄 변경**
모든 `src/main/kotlin/com/dh/ondot/schedule/presentation/` 하위 파일에서:
```
com.dh.ondot.schedule.api → com.dh.ondot.schedule.presentation
```
대상 파일 (~29개):
- Controllers: AlarmController, PlaceController, ScheduleController
- request/ (11): AlarmSwitchRequest, EstimateTimeRequest, PlaceDto, PlaceHistoryDeleteRequest, PlaceHistorySaveRequest, QuickScheduleCreateRequest, RecordAlarmTriggerRequest, ScheduleCreateRequest, ScheduleParsedRequest, ScheduleUpdateRequest, SetAlarmRequest
- response/ (12): AlarmDto, AlarmSwitchResponse, EstimateTimeResponse, HomeScheduleListResponse, PlaceHistoryResponse, PlaceSearchResponse, ScheduleCreateResponse, ScheduleDetailResponse, ScheduleParsedResponse, SchedulePreparationResponse, ScheduleUpdateResponse, SettingAlarmResponse
- swagger/ (3): AlarmSwagger, PlaceSwagger, ScheduleSwagger

**Step 3: 외부 import 경로 변경**
`schedule.api.request` 또는 `schedule.api.response`를 import하는 파일들:
- `schedule/application/ScheduleCommandFacade.kt` — `schedule.api.request.*` import 변경
- `schedule/application/ScheduleQueryFacade.kt` — `schedule.api.response.*` import 변경 (있다면)
- `schedule/application/dto/HomeScheduleListItem.kt` — `schedule.api.response.AlarmDto` import 변경 (있다면)
- `schedule/infra/api/OpenAiPromptApi.kt` — `schedule.api.response.*` import 변경 (있다면)

Grep으로 `schedule.api.request\|schedule.api.response\|schedule.api.swagger`를 검색하여 presentation 패키지 외부의 모든 import를 찾아 변경할 것.

**Step 4: 빌드 검증**
```bash
export JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home && ./gradlew clean build 2>&1 | tail -20
```

**Step 5: 커밋**
```bash
git add -A && git commit -m "refactor: schedule/api 패키지를 schedule/presentation으로 rename"
```

---

## Task 3: AuthController 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/member/presentation/AuthControllerTest.kt`

**엔드포인트 (4개):**
| Method | Path | 인증 | 응답 |
|--------|------|------|------|
| POST | /auth/login/oauth | 불필요 | 200 LoginResponse |
| POST | /auth/reissue | 불필요 (자체 토큰 추출) | 200 Token |
| POST | /auth/logout | 불필요 (자체 토큰 추출) | 204 |
| POST | /auth/test/token | 불필요 | 200 AccessToken |

**테스트 케이스:**
1. `loginWithOAuth_정상_200`: provider=KAKAO, access_token 전달 → 200 + LoginResponse JSON 검증
2. `loginWithOAuth_provider누락_400`: provider 파라미터 누락 → 400
3. `loginWithOAuth_accessToken누락_400`: access_token 파라미터 누락 → 400
4. `reissue_정상_200`: Authorization 헤더 Bearer 토큰 → 200 + Token JSON
5. `reissue_토큰헤더누락_예외`: Authorization 없이 → TokenMissingException (400/401)
6. `logout_정상_204`: Authorization 헤더 → 204
7. `logout_잘못된토큰_204`: 잘못된 형식이어도 catch로 204 (로직상 예외 무시)
8. `testToken_정상_200`: POST → 200 + AccessToken JSON

**주의:** AuthController는 `/auth/**` 경로이므로 TokenInterceptor 패턴 밖. 인증 불필요.

**Step 1: 테스트 파일 작성**

```kotlin
package com.dh.ondot.member.presentation

import com.dh.ondot.member.application.AuthFacade
import com.dh.ondot.member.application.TokenFacade
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.core.TokenExtractor
import com.dh.ondot.member.core.exception.TokenMissingException
import com.dh.ondot.member.presentation.response.AccessToken
import com.dh.ondot.member.presentation.response.LoginResponse
import com.dh.ondot.member.domain.enums.OauthProvider
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.bean.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @MockBean private lateinit var authFacade: AuthFacade
    @MockBean private lateinit var tokenFacade: TokenFacade

    // ... 8개 테스트 메서드
}
```

**Step 2: 빌드 및 테스트 실행**
```bash
export JAVA_HOME=... && ./gradlew test --tests "com.dh.ondot.member.presentation.AuthControllerTest" 2>&1 | tail -20
```

**Step 3: 커밋**
```bash
git add -A && git commit -m "test: AuthController @WebMvcTest 슬라이스 테스트 작성"
```

---

## Task 4: MemberController 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/member/presentation/MemberControllerTest.kt`

**엔드포인트 (7개):**
| Method | Path | 인증 | 응답 |
|--------|------|------|------|
| DELETE | /members | 필요 | 204 |
| POST | /members/onboarding | 필요 | 200 OnboardingResponse |
| GET | /members/home-address | 필요 | 200 HomeAddressResponse |
| GET | /members/map-provider | 필요 | 200 MapProviderResponse |
| PATCH | /members/map-provider | 필요 | 200 MapProviderResponse |
| PATCH | /members/home-address | 필요 | 200 UpdateHomeAddressResponse |
| GET | /members/preparation-time | 필요 | 200 PreparationTimeResponse |
| PATCH | /members/preparation-time | 필요 | 200 PreparationTimeResponse |

**테스트 케이스 (~20개+):**
- 각 엔드포인트 Happy Path (8개)
- `deleteMember_withdrawalReasonId누락_400`
- `onboarding_preparationTime범위초과_400` (Min/Max 검증)
- `onboarding_roadAddress빈값_400` (NotBlank 검증)
- `updateHomeAddress_longitude범위초과_400` (DecimalMin/Max 검증)
- `updateMapProvider_빈값_400`
- `updatePreparationTime_범위초과_400`
- 인증 없이 접근 시 처리 확인

**인증 처리:**
- `@MockBean TokenInterceptor` + `@MockBean WebConfig` 등록
- Happy Path: `mockMvc.perform(get(...).requestAttr("memberId", 1L))`
- `MemberFacade`를 `@MockBean`으로 등록

**Step 1: 테스트 파일 작성**
**Step 2: 빌드 및 테스트 실행**
**Step 3: 커밋**
```bash
git add -A && git commit -m "test: MemberController @WebMvcTest 슬라이스 테스트 작성"
```

---

## Task 5: ScheduleController 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/schedule/presentation/ScheduleControllerTest.kt`

**엔드포인트 (12개):**
| Method | Path | 인증 | 응답 |
|--------|------|------|------|
| POST | /schedules | 필요 | 201 ScheduleCreateResponse |
| POST | /schedules/quick | 필요 | 202 |
| POST | /schedules/quickV1 | 필요 | 202 |
| POST | /schedules/voice | 필요 | 200 ScheduleParsedResponse |
| POST | /schedules/estimate-time | 필요 | 200 EstimateTimeResponse |
| GET | /schedules/{id} | 필요 | 200 ScheduleDetailResponse |
| GET | /schedules/{id}/preparation | 불필요 (제외 패턴) | 200 SchedulePreparationResponse |
| GET | /schedules/{id}/issues | 불필요 (제외 패턴) | 200 String |
| GET | /schedules | 필요 | 200 HomeScheduleListResponse |
| PUT | /schedules/{id} | 필요 | 200/202 ScheduleUpdateResponse |
| PATCH | /schedules/{id}/alarm | 필요 | 200 AlarmSwitchResponse |
| DELETE | /schedules/{id} | 필요 | 204 |

**테스트 케이스 (~30개+):**
- 각 엔드포인트 Happy Path (12개)
- `createSchedule_title빈값_400`
- `createSchedule_departurePlace누락_400`
- `createQuickSchedule_appointmentAt누락_400`
- `parseVoiceSchedule_text빈값_400`
- `estimateTravelTime_좌표범위초과_400`
- `getSchedule_존재하지않는스케줄_404` (NotFoundScheduleException)
- `updateSchedule_title빈값_400`
- `switchAlarm_isEnabled누락_400`
- `updateSchedule_결과에따라_200또는202` (needsDepartureTimeRecalculation)

**MockBean 대상:**
- `ScheduleQueryFacade`, `ScheduleCommandFacade`, `RouteService`
- `TokenInterceptor`, `WebConfig`

**Step 1: 테스트 파일 작성**
**Step 2: 빌드 및 테스트 실행**
**Step 3: 커밋**
```bash
git add -A && git commit -m "test: ScheduleController @WebMvcTest 슬라이스 테스트 작성"
```

---

## Task 6: AlarmController 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/schedule/presentation/AlarmControllerTest.kt`

**엔드포인트 (2개):**
| Method | Path | 인증 | 응답 |
|--------|------|------|------|
| POST | /alarms/setting | 필요 | 200 SettingAlarmResponse |
| POST | /alarms/triggers | 필요 | 201 |

**테스트 케이스 (~8개):**
- `setAlarm_정상_200`
- `setAlarm_appointmentAt누락_400`
- `setAlarm_좌표범위초과_400`
- `recordAlarmTrigger_정상_201`
- `recordAlarmTrigger_scheduleId누락_400`
- `recordAlarmTrigger_잘못된action_400` (Pattern 검증)
- `recordAlarmTrigger_alarmId누락_400`

**MockBean 대상:**
- `AlarmFacade`, `AlarmService`
- `TokenInterceptor`, `WebConfig`

**Step 1: 테스트 파일 작성**
**Step 2: 빌드 및 테스트 실행**
**Step 3: 커밋**
```bash
git add -A && git commit -m "test: AlarmController @WebMvcTest 슬라이스 테스트 작성"
```

---

## Task 7: PlaceController 테스트 작성

**Files:**
- Create: `src/test/kotlin/com/dh/ondot/schedule/presentation/PlaceControllerTest.kt`

**엔드포인트 (4개):**
| Method | Path | 인증 | 응답 |
|--------|------|------|------|
| GET | /places/search?query=... | 필요 | 200 List<PlaceSearchResponse> |
| POST | /places/history | 필요 | 201 |
| GET | /places/history | 필요 | 200 List<PlaceHistoryResponse> |
| DELETE | /places/history | 필요 | 204 |

**테스트 케이스 (~10개):**
- `searchPlaces_정상_200`
- `searchPlaces_query빈값_400` (ConstraintViolation — @Validated + @NotBlank)
- `searchPlaces_query누락_400`
- `saveHistory_정상_201`
- `saveHistory_roadAddress빈값_400`
- `saveHistory_좌표범위초과_400`
- `getHistory_정상_200`
- `deleteHistory_정상_204`
- `deleteHistory_searchedAt누락_400`

**MockBean 대상:**
- `PlaceFacade`
- `TokenInterceptor`, `WebConfig`

**주의:** PlaceController는 `@Validated` 클래스 레벨 어노테이션이 있어 `@RequestParam`에 대한 validation이 `ConstraintViolationException`으로 처리됨.

**Step 1: 테스트 파일 작성**
**Step 2: 빌드 및 테스트 실행**
**Step 3: 커밋**
```bash
git add -A && git commit -m "test: PlaceController @WebMvcTest 슬라이스 테스트 작성"
```

---

## Task 8: 전체 빌드 검증 + 최종 커밋

**Step 1: 전체 빌드**
```bash
export JAVA_HOME=... && ./gradlew clean build 2>&1 | tail -30
```

**Step 2: 테스트 수 확인**
기존 83개 + 새로 추가된 테스트 (~70개+) = 150개+ 예상

**Step 3: 빌드 성공하면 push**
```bash
git push origin refactor/DH-5
```

**Step 4: PR 업데이트**
PR title과 body를 업데이트하여 presentation rename + API 테스트 추가 내용 반영.
