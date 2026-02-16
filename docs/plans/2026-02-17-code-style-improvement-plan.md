# 코드 스타일 개선 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** baro 프로젝트 패턴을 참고하여 ondot 코드 품질 6가지(A-F)를 개선한다.

**Architecture:** Controller→Facade→Service 레이어 아키텍처에서 (1) 컨트롤러는 Facade만 의존하도록 정리, (2) Facade는 presentation 레이어에 의존하지 않도록 Command/Result 패턴 도입, (3) 예외 핸들링 보강. 진행 순서: E+F → A → D → B+C.

**Tech Stack:** Kotlin, Spring Boot 3.4, JUnit 5, MockMvc, Mockito

---

### Task 1: ErrorCode에 MISSING_REQUEST_HEADER 추가 (개선 F)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt:7` (Common 섹션에 추가)

**Step 1: ErrorCode에 항목 추가**

`ErrorCode.kt`의 Common 섹션 (`ALREADY_DISCONNECTED` 뒤)에 다음 한 줄 추가:

```kotlin
MISSING_REQUEST_HEADER(BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
```

정확한 위치: `ALREADY_DISCONNECTED(BAD_REQUEST, "이미 클라이언트에서 요청이 종료되었습니다."),` 다음 줄.

**Step 2: 빌드 확인**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt
git commit -m "refactor: MISSING_REQUEST_HEADER ErrorCode 추가 (DH-5)"
```

---

### Task 2: GlobalExceptionHandler에 MissingRequestHeaderException 핸들러 추가 (개선 E)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/GlobalExceptionHandler.kt`
- Modify: `src/test/kotlin/com/dh/ondot/member/presentation/AuthControllerTest.kt`

**Step 1: GlobalExceptionHandler에 핸들러 추가**

`GlobalExceptionHandler.kt`에 import 추가:
```kotlin
import org.springframework.web.bind.MissingRequestHeaderException
```

`handleMissingParam` 메서드 바로 아래에 다음 핸들러 추가:

```kotlin
@ExceptionHandler(MissingRequestHeaderException::class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
fun handleMissingRequestHeader(e: MissingRequestHeaderException): ErrorResponse {
    log.warn(e.message)
    return ErrorResponse(ErrorCode.MISSING_REQUEST_HEADER)
}
```

**Step 2: AuthControllerTest 수정 — 기존 500 테스트를 400으로 변경**

`AuthControllerTest.kt`에서 `reissue_missingAuthHeader_500` 테스트를 수정:

Before:
```kotlin
@Test
@DisplayName("토큰 재발급 시 Authorization 헤더 누락하면 500을 반환한다")
fun reissue_missingAuthHeader_500() {
    mockMvc.perform(post("/auth/reissue"))
        .andExpect(status().isInternalServerError)
}
```

After:
```kotlin
@Test
@DisplayName("토큰 재발급 시 Authorization 헤더 누락하면 400을 반환한다")
fun reissue_missingAuthHeader_400() {
    mockMvc.perform(post("/auth/reissue"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_HEADER"))
}
```

**Step 3: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/core/exception/GlobalExceptionHandler.kt src/test/kotlin/com/dh/ondot/member/presentation/AuthControllerTest.kt
git commit -m "refactor: MissingRequestHeaderException 핸들러 추가 (DH-5)"
```

---

### Task 3: AuthController.logout() try-catch를 TokenFacade로 이동 (개선 A)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/AuthController.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/TokenFacade.kt`
- Modify: `src/test/kotlin/com/dh/ondot/member/presentation/AuthControllerTest.kt`

**Step 1: TokenFacade에 `logoutByHeader` 메서드 추가**

`TokenFacade.kt`에 import 2개 추가:
```kotlin
import com.dh.ondot.member.core.TokenExtractor
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException
import com.dh.ondot.member.core.exception.TokenMissingException
```

`logout` 메서드 바로 아래에 새 메서드 추가:

```kotlin
fun logoutByHeader(authorizationHeader: String) {
    try {
        val refreshToken = TokenExtractor.extract(authorizationHeader)
        logout(refreshToken)
    } catch (_: TokenMissingException) {
    } catch (_: InvalidTokenHeaderException) {
    }
}
```

**Step 2: AuthController.logout() 단순화**

`AuthController.kt`에서 logout 메서드를 다음으로 교체:

Before:
```kotlin
@ResponseStatus(HttpStatus.NO_CONTENT)
@PostMapping("/logout")
override fun logout(
    @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
) {
    try {
        val refreshToken = TokenExtractor.extract(token)
        tokenFacade.logout(refreshToken)
    } catch (_: TokenMissingException) {
    } catch (_: InvalidTokenHeaderException) {
    }
}
```

After:
```kotlin
@ResponseStatus(HttpStatus.NO_CONTENT)
@PostMapping("/logout")
override fun logout(
    @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
) {
    tokenFacade.logoutByHeader(token)
}
```

그리고 `AuthController.kt`에서 더 이상 사용하지 않는 import 제거:
```kotlin
import com.dh.ondot.member.core.TokenExtractor          // 삭제
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException  // 삭제
import com.dh.ondot.member.core.exception.TokenMissingException        // 삭제
```

**Step 3: AuthControllerTest에서 logout 테스트의 verify 대상 수정**

`AuthControllerTest.kt`의 `logout_success_204` 테스트에서:

Before:
```kotlin
verify(tokenFacade).logout("some-refresh-token")
```

After:
```kotlin
verify(tokenFacade).logoutByHeader("Bearer some-refresh-token")
```

**Step 4: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 5: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/member/presentation/AuthController.kt src/main/kotlin/com/dh/ondot/member/application/TokenFacade.kt src/test/kotlin/com/dh/ondot/member/presentation/AuthControllerTest.kt
git commit -m "refactor: AuthController.logout() try-catch를 TokenFacade로 이동 (DH-5)"
```

---

### Task 4: ScheduleController에서 RouteService 직접 의존 제거 (개선 D-1)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/ScheduleController.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleQueryFacade.kt`
- Modify: `src/test/kotlin/com/dh/ondot/schedule/presentation/ScheduleControllerTest.kt`

**Step 1: ScheduleQueryFacade에 `estimateTravelTime` 메서드 추가**

`ScheduleQueryFacade.kt`에 import 추가:
```kotlin
import com.dh.ondot.schedule.domain.service.RouteService
```

생성자에 `RouteService` 추가:
```kotlin
class ScheduleQueryFacade(
    private val memberService: MemberService,
    private val scheduleService: ScheduleService,
    private val scheduleQueryService: ScheduleQueryService,
    private val emergencyAlertService: EmergencyAlertService,
    private val homeScheduleListItemMapper: HomeScheduleListItemMapper,
    private val routeService: RouteService,
)
```

클래스 마지막에 새 메서드 추가:
```kotlin
fun estimateTravelTime(
    startLongitude: Double, startLatitude: Double,
    endLongitude: Double, endLatitude: Double,
): Int {
    return routeService.calculateRouteTime(startLongitude, startLatitude, endLongitude, endLatitude)
}
```

**Step 2: ScheduleController에서 RouteService 의존 제거 + estimateTravelTime 수정**

`ScheduleController.kt` 생성자에서 `routeService` 제거:

Before:
```kotlin
class ScheduleController(
    private val scheduleQueryFacade: ScheduleQueryFacade,
    private val scheduleCommandFacade: ScheduleCommandFacade,
    private val routeService: RouteService,
) : ScheduleSwagger {
```

After:
```kotlin
class ScheduleController(
    private val scheduleQueryFacade: ScheduleQueryFacade,
    private val scheduleCommandFacade: ScheduleCommandFacade,
) : ScheduleSwagger {
```

`estimateTravelTime` 메서드 수정:

Before:
```kotlin
@ResponseStatus(HttpStatus.OK)
@PostMapping("/estimate-time")
override fun estimateTravelTime(
    @Valid @RequestBody request: EstimateTimeRequest,
): EstimateTimeResponse {
    val estimatedTime = routeService.calculateRouteTime(
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
    )

    return EstimateTimeResponse.from(estimatedTime)
}
```

After:
```kotlin
@ResponseStatus(HttpStatus.OK)
@PostMapping("/estimate-time")
override fun estimateTravelTime(
    @Valid @RequestBody request: EstimateTimeRequest,
): EstimateTimeResponse {
    val estimatedTime = scheduleQueryFacade.estimateTravelTime(
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
    )

    return EstimateTimeResponse.from(estimatedTime)
}
```

그리고 `ScheduleController.kt`에서 더 이상 사용하지 않는 import 제거:
```kotlin
import com.dh.ondot.schedule.domain.service.RouteService  // 삭제
```

**Step 3: ScheduleControllerTest 수정**

`ScheduleControllerTest.kt`에서:
- `@MockitoBean private lateinit var routeService: RouteService` 삭제
- `routeService` import (`com.dh.ondot.schedule.domain.service.RouteService`) 삭제
- `estimateTravelTime` 테스트의 mock 대상을 `routeService.calculateRouteTime` → `scheduleQueryFacade.estimateTravelTime`으로 변경

estimateTravelTime 테스트의 given 변경:

Before:
```kotlin
whenever(routeService.calculateRouteTime(any(), any(), any(), any())).thenReturn(35)
```

After:
```kotlin
whenever(scheduleQueryFacade.estimateTravelTime(any(), any(), any(), any())).thenReturn(35)
```

**Step 4: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 5: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/presentation/ScheduleController.kt src/main/kotlin/com/dh/ondot/schedule/application/ScheduleQueryFacade.kt src/test/kotlin/com/dh/ondot/schedule/presentation/ScheduleControllerTest.kt
git commit -m "refactor: ScheduleController에서 RouteService 직접 의존 제거 (DH-5)"
```

---

### Task 5: AlarmController에서 AlarmService 직접 의존 제거 (개선 D-2)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/AlarmController.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/AlarmFacade.kt`
- Modify: `src/test/kotlin/com/dh/ondot/schedule/presentation/AlarmControllerTest.kt`

**Step 1: AlarmFacade에 `recordAlarmTrigger` 메서드 추가**

`AlarmFacade.kt`에 import 추가:
```kotlin
import com.dh.ondot.schedule.domain.service.AlarmService
```

생성자에 `AlarmService` 추가:
```kotlin
class AlarmFacade(
    private val memberService: MemberService,
    private val routeService: RouteService,
    private val scheduleService: ScheduleService,
    private val alarmService: AlarmService,
)
```

클래스 마지막에 새 메서드 추가:
```kotlin
fun recordAlarmTrigger(
    memberId: Long,
    alarmId: Long,
    scheduleId: Long,
    action: String,
    mobileType: String,
) {
    alarmService.recordTrigger(memberId, alarmId, scheduleId, action, mobileType)
}
```

**Step 2: AlarmController에서 AlarmService 의존 제거 + recordAlarmTrigger 수정**

`AlarmController.kt` 생성자에서 `alarmService` 제거:

Before:
```kotlin
class AlarmController(
    private val alarmFacade: AlarmFacade,
    private val alarmService: AlarmService,
) : AlarmSwagger {
```

After:
```kotlin
class AlarmController(
    private val alarmFacade: AlarmFacade,
) : AlarmSwagger {
```

`recordAlarmTrigger` 메서드 수정:

Before:
```kotlin
@ResponseStatus(HttpStatus.CREATED)
@PostMapping("/triggers")
override fun recordAlarmTrigger(
    @RequestAttribute("memberId") memberId: Long,
    @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String?,
    @Valid @RequestBody request: RecordAlarmTriggerRequest,
) {
    alarmService.recordTrigger(
        memberId,
        request.alarmId,
        request.scheduleId,
        request.action,
        mobileType ?: "",
    )
}
```

After:
```kotlin
@ResponseStatus(HttpStatus.CREATED)
@PostMapping("/triggers")
override fun recordAlarmTrigger(
    @RequestAttribute("memberId") memberId: Long,
    @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String?,
    @Valid @RequestBody request: RecordAlarmTriggerRequest,
) {
    alarmFacade.recordAlarmTrigger(
        memberId,
        request.alarmId,
        request.scheduleId,
        request.action,
        mobileType ?: "",
    )
}
```

그리고 `AlarmController.kt`에서 더 이상 사용하지 않는 import 제거:
```kotlin
import com.dh.ondot.schedule.domain.service.AlarmService  // 삭제
```

**Step 3: AlarmControllerTest 수정**

`AlarmControllerTest.kt`에서:
- `@MockitoBean private lateinit var alarmService: AlarmService` 삭제
- `alarmService` import (`com.dh.ondot.schedule.domain.service.AlarmService`) 삭제
- `recordAlarmTrigger` 테스트에서 verify 대상을 `alarmService.recordTrigger` → `alarmFacade.recordAlarmTrigger`로 변경

**Step 4: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 5: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/presentation/AlarmController.kt src/main/kotlin/com/dh/ondot/schedule/application/AlarmFacade.kt src/test/kotlin/com/dh/ondot/schedule/presentation/AlarmControllerTest.kt
git commit -m "refactor: AlarmController에서 AlarmService 직접 의존 제거 (DH-5)"
```

---

### Task 6: OnboardingRequest→Command 변환 패턴 도입 + MemberFacade 정리 (개선 B+C — member)

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/request/OnboardingRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/command/OnboardingCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/command/CreateAddressCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/command/CreateChoicesCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/MemberFacade.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/MemberController.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/application/dto/LoginResult.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/application/AuthFacade.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/application/dto/OnboardingResult.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/response/LoginResponse.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/response/OnboardingResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/application/command/DeleteMemberCommand.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/application/command/UpdateHomeAddressCommand.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/application/command/SavePlaceHistoryCommand.kt`

**Step 1: Command 클래스에서 OnboardingRequest import 제거 — OnboardingRequest에 toCommand() 추가**

`OnboardingRequest.kt`에 import 3개 추가:
```kotlin
import com.dh.ondot.member.application.command.OnboardingCommand
import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.application.command.CreateChoicesCommand
```

`OnboardingRequest` data class 본문 닫는 괄호 바로 위에 다음 추가:
```kotlin
) {
    // 기존 QuestionDto...

    fun toOnboardingCommand(): OnboardingCommand =
        OnboardingCommand(
            preparationTime = preparationTime,
            alarmMode = alarmMode,
            isSnoozeEnabled = isSnoozeEnabled,
            snoozeInterval = snoozeInterval,
            snoozeCount = snoozeCount,
            soundCategory = soundCategory,
            ringTone = ringTone,
            volume = volume,
        )

    fun toAddressCommand(): CreateAddressCommand =
        CreateAddressCommand(
            roadAddress = roadAddress,
            longitude = longitude,
            latitude = latitude,
        )

    fun toChoicesCommand(): CreateChoicesCommand =
        CreateChoicesCommand(
            questionAnswerPairs = questions.map { q ->
                CreateChoicesCommand.QuestionAnswerPair(q.questionId, q.answerId)
            }
        )
}
```

**Step 2: OnboardingCommand.kt에서 `companion object` + `from(request)` 제거**

`OnboardingCommand.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.application.command

data class OnboardingCommand(
    val preparationTime: Int,
    val alarmMode: String,
    val isSnoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val snoozeCount: Int,
    val soundCategory: String,
    val ringTone: String,
    val volume: Double,
)
```

**Step 3: CreateAddressCommand.kt에서 `companion object` + `from(request)` 제거**

`CreateAddressCommand.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.application.command

data class CreateAddressCommand(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
```

**Step 4: CreateChoicesCommand.kt에서 `companion object` + `from(request)` 제거**

`CreateChoicesCommand.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.application.command

data class CreateChoicesCommand(
    val questionAnswerPairs: List<QuestionAnswerPair>,
) {
    data class QuestionAnswerPair(
        val questionId: Long,
        val answerId: Long,
    )
}
```

**Step 5: LoginResult application DTO 생성**

새 파일 `src/main/kotlin/com/dh/ondot/member/application/dto/LoginResult.kt`:
```kotlin
package com.dh.ondot.member.application.dto

data class LoginResult(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
)
```

**Step 6: OnboardingResult application DTO 생성**

새 파일 `src/main/kotlin/com/dh/ondot/member/application/dto/OnboardingResult.kt`:
```kotlin
package com.dh.ondot.member.application.dto

import java.time.LocalDateTime

data class OnboardingResult(
    val accessToken: String,
    val refreshToken: String,
    val createdAt: LocalDateTime,
)
```

**Step 7: DeleteMemberCommand 생성**

새 파일 `src/main/kotlin/com/dh/ondot/member/application/command/DeleteMemberCommand.kt`:
```kotlin
package com.dh.ondot.member.application.command

data class DeleteMemberCommand(
    val withdrawalReasonId: Long,
    val customReason: String?,
)
```

**Step 8: UpdateHomeAddressCommand 생성**

새 파일 `src/main/kotlin/com/dh/ondot/member/application/command/UpdateHomeAddressCommand.kt`:
```kotlin
package com.dh.ondot.member.application.command

data class UpdateHomeAddressCommand(
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
```

**Step 9: AuthFacade에서 LoginResponse 의존 제거 → LoginResult 반환**

`AuthFacade.kt` 전체를 다음으로 교체:
```kotlin
package com.dh.ondot.member.application

import com.dh.ondot.member.application.dto.LoginResult
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.domain.OauthApiFactory
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.service.MemberService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthFacade(
    private val tokenFacade: TokenFacade,
    private val oauthApiFactory: OauthApiFactory,
    private val memberService: MemberService,
) {
    private val log = LoggerFactory.getLogger(AuthFacade::class.java)

    @Transactional
    fun loginWithOAuth(oauthProvider: OauthProvider, accessToken: String): LoginResult {
        val oauthApi = oauthApiFactory.getOauthApi(oauthProvider)
        val userInfo = oauthApi.fetchUser(accessToken)

        val member = memberService.findOrRegisterOauthMember(userInfo, oauthProvider)

        val isNewMember = member.isNewMember()
        val token: Token = tokenFacade.issue(member.id)
        return if (isNewMember) {
            LoginResult(member.id, token.accessToken, "", true)
        } else {
            LoginResult(member.id, token.accessToken, token.refreshToken, false)
        }
    }
}
```

**Step 10: LoginResponse에 `from(LoginResult)` 팩토리 추가**

`LoginResponse.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.presentation.response

import com.dh.ondot.member.application.dto.LoginResult

data class LoginResponse(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
) {
    companion object {
        fun from(result: LoginResult): LoginResponse =
            LoginResponse(result.memberId, result.accessToken, result.refreshToken, result.isNewMember)
    }
}
```

**Step 11: MemberFacade에서 presentation 의존 제거 → Command 패턴 적용 + OnboardingResult 반환**

`MemberFacade.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.application.command.CreateAddressCommand
import com.dh.ondot.member.application.command.CreateChoicesCommand
import com.dh.ondot.member.application.command.DeleteMemberCommand
import com.dh.ondot.member.application.command.OnboardingCommand
import com.dh.ondot.member.application.command.UpdateHomeAddressCommand
import com.dh.ondot.member.application.dto.OnboardingResult
import com.dh.ondot.member.application.dto.Token
import com.dh.ondot.member.domain.Address
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.event.UserRegistrationEvent
import com.dh.ondot.member.domain.service.AddressService
import com.dh.ondot.member.domain.service.ChoiceService
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.member.domain.service.WithdrawalService
import com.dh.ondot.schedule.domain.service.ScheduleService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberFacade(
    private val tokenFacade: TokenFacade,
    private val memberService: MemberService,
    private val addressService: AddressService,
    private val choiceService: ChoiceService,
    private val scheduleService: ScheduleService,
    private val withdrawalService: WithdrawalService,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun getMember(memberId: Long): Member =
        memberService.getMemberIfExists(memberId)

    @Transactional(readOnly = true)
    fun getHomeAddress(memberId: Long): Address {
        memberService.getMemberIfExists(memberId)
        return addressService.getHomeAddress(memberId)
    }

    @Transactional
    fun onboarding(
        memberId: Long,
        mobileType: String,
        onboardingCommand: OnboardingCommand,
        addressCommand: CreateAddressCommand,
        choicesCommand: CreateChoicesCommand,
    ): OnboardingResult {
        val member = memberService.getAndValidateAlreadyOnboarded(memberId)

        memberService.updateOnboardingInfo(member, onboardingCommand)
        addressService.createHomeAddress(member, addressCommand)
        choiceService.createChoices(member, choicesCommand)

        val token: Token = tokenFacade.issue(member.id)

        publishUserRegistrationEvent(member, member.oauthInfo.oauthProvider, mobileType)

        return OnboardingResult(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            createdAt = TimeUtils.toSeoulDateTime(member.updatedAt)!!,
        )
    }

    private fun publishUserRegistrationEvent(member: Member, oauthProvider: OauthProvider, mobileType: String) {
        val totalMemberCount = memberService.getTotalMemberCount()

        val event = UserRegistrationEvent(
            member.id,
            member.email,
            oauthProvider,
            totalMemberCount,
            mobileType,
        )

        eventPublisher.publishEvent(event)
    }

    @Transactional
    fun updateMapProvider(memberId: Long, mapProvider: String): Member {
        val member = memberService.getMemberIfExists(memberId)
        member.updateMapProvider(mapProvider)

        return member
    }

    @Transactional
    fun updateHomeAddress(memberId: Long, command: UpdateHomeAddressCommand): Address {
        memberService.getMemberIfExists(memberId)
        val addressCommand = CreateAddressCommand(command.roadAddress, command.longitude, command.latitude)
        return addressService.updateHomeAddress(memberId, addressCommand)
    }

    fun updatePreparationTime(memberId: Long, preparationTime: Int): Member =
        memberService.updatePreparationTime(memberId, preparationTime)

    @Transactional
    fun deleteMember(memberId: Long, command: DeleteMemberCommand) {
        memberService.getMemberIfExists(memberId)
        withdrawalService.saveWithdrawalReason(memberId, command.withdrawalReasonId, command.customReason)

        scheduleService.deleteAllByMemberId(memberId)
        addressService.deleteAllByMemberId(memberId)
        choiceService.deleteAllByMemberId(memberId)
        memberService.deleteMember(memberId)
    }
}
```

**Step 12: OnboardingResponse에서 Member 의존 제거 → OnboardingResult에서 변환**

`OnboardingResponse.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.presentation.response

import com.dh.ondot.member.application.dto.OnboardingResult
import java.time.LocalDateTime

data class OnboardingResponse(
    val accessToken: String,
    val refreshToken: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: OnboardingResult): OnboardingResponse =
            OnboardingResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                createdAt = result.createdAt,
            )
    }
}
```

**Step 13: AuthController에서 LoginResponse.from(result) 패턴 적용**

`AuthController.kt`의 `loginWithOAuth` 메서드를 수정:

Before:
```kotlin
): LoginResponse = authFacade.loginWithOAuth(provider, accessToken)
```

After:
```kotlin
): LoginResponse {
    val result = authFacade.loginWithOAuth(provider, accessToken)
    return LoginResponse.from(result)
}
```

**Step 14: MemberController에서 Command 변환 패턴 적용**

`MemberController.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.member.presentation

import com.dh.ondot.member.application.MemberFacade
import com.dh.ondot.member.application.command.DeleteMemberCommand
import com.dh.ondot.member.application.command.UpdateHomeAddressCommand
import com.dh.ondot.member.presentation.request.OnboardingRequest
import com.dh.ondot.member.presentation.request.UpdateHomeAddressRequest
import com.dh.ondot.member.presentation.request.UpdateMapProviderRequest
import com.dh.ondot.member.presentation.request.UpdatePreparationTimeRequest
import com.dh.ondot.member.presentation.request.WithdrawalRequest
import com.dh.ondot.member.presentation.response.HomeAddressResponse
import com.dh.ondot.member.presentation.response.MapProviderResponse
import com.dh.ondot.member.presentation.response.OnboardingResponse
import com.dh.ondot.member.presentation.response.PreparationTimeResponse
import com.dh.ondot.member.presentation.response.UpdateHomeAddressResponse
import com.dh.ondot.member.presentation.swagger.MemberSwagger
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberFacade: MemberFacade,
) : MemberSwagger {

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    override fun deleteMember(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: WithdrawalRequest,
    ) {
        memberFacade.deleteMember(memberId, DeleteMemberCommand(request.withdrawalReasonId, request.customReason))
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/onboarding")
    override fun onboarding(
        @RequestAttribute("memberId") memberId: Long,
        @RequestHeader(value = "X-Mobile-Type", required = false) mobileType: String,
        @Valid @RequestBody request: OnboardingRequest,
    ): OnboardingResponse {
        val result = memberFacade.onboarding(
            memberId, mobileType,
            request.toOnboardingCommand(),
            request.toAddressCommand(),
            request.toChoicesCommand(),
        )
        return OnboardingResponse.from(result)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/home-address")
    override fun getHomeAddress(
        @RequestAttribute("memberId") memberId: Long,
    ): HomeAddressResponse {
        val address = memberFacade.getHomeAddress(memberId)
        return HomeAddressResponse.from(address)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/map-provider")
    override fun getMapProvider(
        @RequestAttribute("memberId") memberId: Long,
    ): MapProviderResponse {
        val member = memberFacade.getMember(memberId)
        return MapProviderResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/map-provider")
    override fun updateMapProvider(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdateMapProviderRequest,
    ): MapProviderResponse {
        val member = memberFacade.updateMapProvider(memberId, request.mapProvider)
        return MapProviderResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/home-address")
    override fun updateHomeAddress(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdateHomeAddressRequest,
    ): UpdateHomeAddressResponse {
        val command = UpdateHomeAddressCommand(request.roadAddress, request.longitude, request.latitude)
        val address = memberFacade.updateHomeAddress(memberId, command)
        return UpdateHomeAddressResponse.from(address)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/preparation-time")
    override fun getPreparationTime(
        @RequestAttribute("memberId") memberId: Long,
    ): PreparationTimeResponse {
        val member = memberFacade.getMember(memberId)
        return PreparationTimeResponse.from(member)
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/preparation-time")
    override fun updatePreparationTime(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: UpdatePreparationTimeRequest,
    ): PreparationTimeResponse {
        val member = memberFacade.updatePreparationTime(memberId, request.preparationTime)
        return PreparationTimeResponse.from(member)
    }
}
```

**Step 15: 빌드 확인**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 16: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL

**Step 17: 커밋**

```bash
git add -A
git commit -m "refactor: member 모듈 Command/Result 패턴 도입 및 Facade→Presentation 의존 제거 (DH-5)"
```

---

### Task 7: ScheduleCommandFacade에서 Request DTO 의존 제거 — toCommand() 패턴 도입 (개선 B+C — schedule)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/command/CreateScheduleCommand.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/command/UpdateScheduleCommand.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/command/CreateQuickScheduleCommand.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/ScheduleCreateRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/ScheduleUpdateRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/request/QuickScheduleCreateRequest.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/application/ScheduleCommandFacade.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/ScheduleController.kt`

**Step 1: CreateScheduleCommand 생성**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/command/CreateScheduleCommand.kt`:
```kotlin
package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class CreateScheduleCommand(
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val isMedicationRequired: Boolean,
    val preparationNote: String?,
    val departurePlace: PlaceInfo,
    val arrivalPlace: PlaceInfo,
    val preparationAlarm: PreparationAlarmInfo,
    val departureAlarm: DepartureAlarmInfo,
) {
    data class PlaceInfo(
        val title: String,
        val roadAddress: String,
        val longitude: Double,
        val latitude: Double,
    )

    data class PreparationAlarmInfo(
        val alarmMode: String,
        val isEnabled: Boolean,
        val triggeredAt: LocalDateTime,
        val isSnoozeEnabled: Boolean,
        val snoozeInterval: Int,
        val snoozeCount: Int,
        val soundCategory: String,
        val ringTone: String,
        val volume: Double,
    )

    data class DepartureAlarmInfo(
        val alarmMode: String,
        val triggeredAt: LocalDateTime,
        val isSnoozeEnabled: Boolean,
        val snoozeInterval: Int,
        val snoozeCount: Int,
        val soundCategory: String,
        val ringTone: String,
        val volume: Double,
    )
}
```

**Step 2: UpdateScheduleCommand 생성**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/command/UpdateScheduleCommand.kt`:
```kotlin
package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class UpdateScheduleCommand(
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
    val preparationAlarm: CreateScheduleCommand.PreparationAlarmInfo,
    val departureAlarm: CreateScheduleCommand.DepartureAlarmInfo,
)
```

**Step 3: CreateQuickScheduleCommand 생성**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/command/CreateQuickScheduleCommand.kt`:
```kotlin
package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class CreateQuickScheduleCommand(
    val appointmentAt: LocalDateTime,
    val departurePlace: CreateScheduleCommand.PlaceInfo,
    val arrivalPlace: CreateScheduleCommand.PlaceInfo,
)
```

**Step 4: ScheduleCreateRequest에 toCommand() 추가**

`ScheduleCreateRequest.kt` data class 닫는 괄호 앞에 메서드 추가. 먼저 import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
```

data class 본문에 추가 (DepartureAlarmDto 닫는 괄호 뒤, ScheduleCreateRequest 닫는 괄호 전):
```kotlin
fun toCommand(): CreateScheduleCommand = CreateScheduleCommand(
    title = title,
    isRepeat = isRepeat,
    repeatDays = repeatDays,
    appointmentAt = appointmentAt,
    isMedicationRequired = isMedicationRequired,
    preparationNote = preparationNote,
    departurePlace = CreateScheduleCommand.PlaceInfo(
        departurePlace.title, departurePlace.roadAddress,
        departurePlace.longitude, departurePlace.latitude,
    ),
    arrivalPlace = CreateScheduleCommand.PlaceInfo(
        arrivalPlace.title, arrivalPlace.roadAddress,
        arrivalPlace.longitude, arrivalPlace.latitude,
    ),
    preparationAlarm = CreateScheduleCommand.PreparationAlarmInfo(
        preparationAlarm.alarmMode, preparationAlarm.isEnabled,
        preparationAlarm.triggeredAt, preparationAlarm.isSnoozeEnabled,
        preparationAlarm.snoozeInterval, preparationAlarm.snoozeCount,
        preparationAlarm.soundCategory, preparationAlarm.ringTone,
        preparationAlarm.volume,
    ),
    departureAlarm = CreateScheduleCommand.DepartureAlarmInfo(
        departureAlarm.alarmMode, departureAlarm.triggeredAt,
        departureAlarm.isSnoozeEnabled, departureAlarm.snoozeInterval,
        departureAlarm.snoozeCount, departureAlarm.soundCategory,
        departureAlarm.ringTone, departureAlarm.volume,
    ),
)
```

**Step 5: ScheduleUpdateRequest에 toCommand() 추가**

`ScheduleUpdateRequest.kt`에 import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.application.command.UpdateScheduleCommand
```

data class 본문에 추가:
```kotlin
fun toCommand(): UpdateScheduleCommand = UpdateScheduleCommand(
    title = title,
    isRepeat = isRepeat,
    repeatDays = repeatDays,
    appointmentAt = appointmentAt,
    departurePlace = CreateScheduleCommand.PlaceInfo(
        departurePlace.title, departurePlace.roadAddress,
        departurePlace.longitude, departurePlace.latitude,
    ),
    arrivalPlace = CreateScheduleCommand.PlaceInfo(
        arrivalPlace.title, arrivalPlace.roadAddress,
        arrivalPlace.longitude, arrivalPlace.latitude,
    ),
    preparationAlarm = CreateScheduleCommand.PreparationAlarmInfo(
        preparationAlarm.alarmMode, preparationAlarm.isEnabled,
        preparationAlarm.triggeredAt, preparationAlarm.isSnoozeEnabled,
        preparationAlarm.snoozeInterval, preparationAlarm.snoozeCount,
        preparationAlarm.soundCategory, preparationAlarm.ringTone,
        preparationAlarm.volume,
    ),
    departureAlarm = CreateScheduleCommand.DepartureAlarmInfo(
        departureAlarm.alarmMode, departureAlarm.triggeredAt,
        departureAlarm.isSnoozeEnabled, departureAlarm.snoozeInterval,
        departureAlarm.snoozeCount, departureAlarm.soundCategory,
        departureAlarm.ringTone, departureAlarm.volume,
    ),
)
```

**Step 6: QuickScheduleCreateRequest에 toCommand() 추가**

`QuickScheduleCreateRequest.kt`에 import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
```

data class 닫는 괄호를 다음으로 교체:
```kotlin
) {
    fun toCommand(): CreateQuickScheduleCommand = CreateQuickScheduleCommand(
        appointmentAt = appointmentAt,
        departurePlace = CreateScheduleCommand.PlaceInfo(
            departurePlace.title, departurePlace.roadAddress,
            departurePlace.longitude, departurePlace.latitude,
        ),
        arrivalPlace = CreateScheduleCommand.PlaceInfo(
            arrivalPlace.title, arrivalPlace.roadAddress,
            arrivalPlace.longitude, arrivalPlace.latitude,
        ),
    )
}
```

**Step 7: ScheduleCommandFacade를 Command 기반으로 리팩토링**

`ScheduleCommandFacade.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.application.command.UpdateScheduleCommand
import com.dh.ondot.schedule.application.dto.ScheduleParsedResult
import com.dh.ondot.schedule.application.dto.UpdateScheduleResult
import com.dh.ondot.schedule.application.mapper.QuickScheduleMapper
import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.service.*
import com.dh.ondot.schedule.infra.api.OpenAiPromptApi
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.TreeSet

@Service
class ScheduleCommandFacade(
    private val memberService: MemberService,
    private val scheduleService: ScheduleService,
    private val scheduleQueryService: ScheduleQueryService,
    private val routeService: RouteService,
    private val placeService: PlaceService,
    private val aiUsageService: AiUsageService,
    private val quickScheduleMapper: QuickScheduleMapper,
    private val openAiPromptApi: OpenAiPromptApi,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun createSchedule(memberId: Long, command: CreateScheduleCommand): Schedule {
        val departurePlace = Place.createPlace(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        val arrivalPlace = Place.createPlace(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val preparationAlarm = Alarm.createPreparationAlarm(
            command.preparationAlarm.alarmMode,
            command.preparationAlarm.isEnabled,
            command.preparationAlarm.triggeredAt,
            command.preparationAlarm.isSnoozeEnabled,
            command.preparationAlarm.snoozeInterval,
            command.preparationAlarm.snoozeCount,
            command.preparationAlarm.soundCategory,
            command.preparationAlarm.ringTone,
            command.preparationAlarm.volume,
        )

        val departureAlarm = Alarm.createDepartureAlarm(
            command.departureAlarm.alarmMode,
            command.departureAlarm.triggeredAt,
            command.departureAlarm.isSnoozeEnabled,
            command.departureAlarm.snoozeInterval,
            command.departureAlarm.snoozeCount,
            command.departureAlarm.soundCategory,
            command.departureAlarm.ringTone,
            command.departureAlarm.volume,
        )

        val schedule = Schedule.createSchedule(
            memberId,
            departurePlace,
            arrivalPlace,
            preparationAlarm,
            departureAlarm,
            command.title,
            command.isRepeat,
            TreeSet(command.repeatDays),
            command.appointmentAt,
            command.isMedicationRequired,
            command.preparationNote,
        )

        return scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickSchedule(memberId: Long, command: CreateQuickScheduleCommand) {
        val member = memberService.getMemberIfExists(memberId)

        val dep = Place.createPlace(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )
        val arr = Place.createPlace(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val estimatedTime = routeService.calculateRouteTime(
            command.departurePlace.longitude, command.departurePlace.latitude,
            command.arrivalPlace.longitude, command.arrivalPlace.latitude,
        )

        val schedule = scheduleService.setupSchedule(
            member, command.appointmentAt, estimatedTime,
        )
        schedule.registerPlaces(dep, arr)

        scheduleService.saveSchedule(schedule)
    }

    @Transactional
    fun createQuickScheduleV1(memberId: Long, command: CreateQuickScheduleCommand) {
        memberService.getMemberIfExists(memberId)
        val cmd = quickScheduleMapper.toCommand(memberId, command)
        val event = placeService.savePlaces(cmd)
        eventPublisher.publishEvent(event)
    }

    @Transactional
    fun updateSchedule(memberId: Long, scheduleId: Long, command: UpdateScheduleCommand): UpdateScheduleResult {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)

        val departureChanged = schedule.departurePlace!!.isPlaceChanged(
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        val arrivalChanged = schedule.arrivalPlace!!.isPlaceChanged(
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        val placeChanged = departureChanged || arrivalChanged
        val timeChanged = schedule.isAppointmentTimeChanged(command.appointmentAt)

        if (placeChanged || timeChanged) {
            // TODO: 경로 재계산·도착/출발 시간 보정 등의 비동기 로직 호출
        }

        schedule.departurePlace!!.update(
            command.departurePlace.title,
            command.departurePlace.roadAddress,
            command.departurePlace.longitude,
            command.departurePlace.latitude,
        )

        schedule.arrivalPlace!!.update(
            command.arrivalPlace.title,
            command.arrivalPlace.roadAddress,
            command.arrivalPlace.longitude,
            command.arrivalPlace.latitude,
        )

        schedule.preparationAlarm!!.updatePreparation(
            command.preparationAlarm.alarmMode,
            command.preparationAlarm.isEnabled,
            command.preparationAlarm.triggeredAt,
            command.preparationAlarm.isSnoozeEnabled,
            command.preparationAlarm.snoozeInterval,
            command.preparationAlarm.snoozeCount,
            command.preparationAlarm.soundCategory,
            command.preparationAlarm.ringTone,
            command.preparationAlarm.volume,
        )

        schedule.departureAlarm!!.updateDeparture(
            command.departureAlarm.alarmMode,
            command.departureAlarm.triggeredAt,
            command.departureAlarm.isSnoozeEnabled,
            command.departureAlarm.snoozeInterval,
            command.departureAlarm.snoozeCount,
            command.departureAlarm.soundCategory,
            command.departureAlarm.ringTone,
            command.departureAlarm.volume,
        )

        schedule.updateCore(
            command.title,
            command.isRepeat,
            TreeSet(command.repeatDays),
            command.appointmentAt,
        )

        return UpdateScheduleResult(schedule, placeChanged || timeChanged)
    }

    @Transactional
    fun parseVoiceSchedule(memberId: Long, sentence: String): ScheduleParsedResult {
        memberService.getMemberIfExists(memberId)
        aiUsageService.increaseUsage(memberId)
        val parsed = openAiPromptApi.parseNaturalLanguage(sentence)
        return ScheduleParsedResult(parsed.departurePlaceTitle, parsed.appointmentAt)
    }

    @Transactional
    fun switchAlarm(
        memberId: Long, scheduleId: Long, enabled: Boolean,
    ): Schedule {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)
        schedule.switchAlarm(enabled)
        return schedule
    }

    fun deleteSchedule(memberId: Long, scheduleId: Long) {
        memberService.getMemberIfExists(memberId)
        val schedule = scheduleQueryService.findScheduleById(scheduleId)
        scheduleService.deleteSchedule(schedule)
    }
}
```

**Step 8: ScheduleParsedResult application DTO 생성**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/dto/ScheduleParsedResult.kt`:
```kotlin
package com.dh.ondot.schedule.application.dto

import java.time.LocalDateTime

data class ScheduleParsedResult(
    val departurePlaceTitle: String?,
    val appointmentAt: LocalDateTime?,
)
```

**Step 9: ScheduleParsedResponse에 from(ScheduleParsedResult) 팩토리 추가**

`ScheduleParsedResponse.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.ScheduleParsedResult
import java.time.LocalDateTime

data class ScheduleParsedResponse(
    val departurePlaceTitle: String?,
    val appointmentAt: LocalDateTime?,
) {
    companion object {
        fun from(result: ScheduleParsedResult): ScheduleParsedResponse =
            ScheduleParsedResponse(result.departurePlaceTitle, result.appointmentAt)
    }
}
```

**Step 10: OpenAiPromptApi에서 ScheduleParsedResponse를 infra 자체 DTO로 교체**

`OpenAiPromptApi.kt`에서 ScheduleParsedResponse를 그대로 사용하되, 이것은 Phase 2 범위이므로 현재는 유지. ScheduleCommandFacade의 `parseVoiceSchedule()`이 ScheduleParsedResult로 변환하여 반환하므로 Facade→Presentation 의존은 이미 끊어진 상태.

**Step 11: QuickScheduleMapper에서 Request DTO 의존 제거**

`QuickScheduleMapper.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.schedule.application.mapper

import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.application.command.QuickScheduleCommand
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.stereotype.Component

@Component
@Mapper(componentModel = "spring")
interface QuickScheduleMapper {
    fun toPlaceInfo(dto: CreateScheduleCommand.PlaceInfo): QuickScheduleCommand.PlaceInfo

    @Mapping(source = "cmd.departurePlace", target = "departure")
    @Mapping(source = "cmd.arrivalPlace", target = "arrival")
    fun toCommand(memberId: Long, cmd: CreateQuickScheduleCommand): QuickScheduleCommand
}
```

**Step 12: ScheduleController에서 toCommand() 호출**

`ScheduleController.kt`를 다음으로 교체:
```kotlin
package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.presentation.request.AlarmSwitchRequest
import com.dh.ondot.schedule.presentation.request.EstimateTimeRequest
import com.dh.ondot.schedule.presentation.request.QuickScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleCreateRequest
import com.dh.ondot.schedule.presentation.request.ScheduleParsedRequest
import com.dh.ondot.schedule.presentation.request.ScheduleUpdateRequest
import com.dh.ondot.schedule.presentation.response.AlarmSwitchResponse
import com.dh.ondot.schedule.presentation.response.EstimateTimeResponse
import com.dh.ondot.schedule.presentation.response.HomeScheduleListResponse
import com.dh.ondot.schedule.presentation.response.ScheduleCreateResponse
import com.dh.ondot.schedule.presentation.response.ScheduleDetailResponse
import com.dh.ondot.schedule.presentation.response.ScheduleParsedResponse
import com.dh.ondot.schedule.presentation.response.SchedulePreparationResponse
import com.dh.ondot.schedule.presentation.response.ScheduleUpdateResponse
import com.dh.ondot.schedule.presentation.swagger.ScheduleSwagger
import com.dh.ondot.schedule.application.ScheduleCommandFacade
import com.dh.ondot.schedule.application.ScheduleQueryFacade
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schedules")
class ScheduleController(
    private val scheduleQueryFacade: ScheduleQueryFacade,
    private val scheduleCommandFacade: ScheduleCommandFacade,
) : ScheduleSwagger {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    override fun createSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleCreateRequest,
    ): ScheduleCreateResponse {
        val schedule = scheduleCommandFacade.createSchedule(memberId, request.toCommand())

        return ScheduleCreateResponse.of(schedule)
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quick")
    override fun createQuickSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickSchedule(memberId, request.toCommand())
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/quickV1")
    fun createQuickScheduleV1(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: QuickScheduleCreateRequest,
    ) {
        scheduleCommandFacade.createQuickScheduleV1(memberId, request.toCommand())
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/voice")
    override fun parseVoiceSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: ScheduleParsedRequest,
    ): ScheduleParsedResponse {
        val result = scheduleCommandFacade.parseVoiceSchedule(memberId, request.text)
        return ScheduleParsedResponse.from(result)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/estimate-time")
    override fun estimateTravelTime(
        @Valid @RequestBody request: EstimateTimeRequest,
    ): EstimateTimeResponse {
        val estimatedTime = scheduleQueryFacade.estimateTravelTime(
            request.startLongitude, request.startLatitude,
            request.endLongitude, request.endLatitude,
        )

        return EstimateTimeResponse.from(estimatedTime)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}")
    override fun getSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ): ScheduleDetailResponse {
        val schedule = scheduleQueryFacade.findOneByMemberAndSchedule(memberId, scheduleId)

        return ScheduleDetailResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/preparation")
    override fun getPreparationInfo(
        @PathVariable scheduleId: Long,
    ): SchedulePreparationResponse {
        val schedule = scheduleQueryFacade.findOne(scheduleId)

        return SchedulePreparationResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{scheduleId}/issues")
    override fun getScheduleIssues(
        @PathVariable scheduleId: Long,
    ): String {
        return scheduleQueryFacade.getIssues(scheduleId)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    override fun getActiveSchedules(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): HomeScheduleListResponse {
        val pageable = PageRequest.of(page, size)
        return scheduleQueryFacade.findAllActiveSchedules(memberId, pageable)
    }

    @PutMapping("/{scheduleId}")
    override fun updateSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: ScheduleUpdateRequest,
    ): ResponseEntity<ScheduleUpdateResponse> {
        val result = scheduleCommandFacade.updateSchedule(memberId, scheduleId, request.toCommand())
        val status = if (result.needsDepartureTimeRecalculation) HttpStatus.ACCEPTED else HttpStatus.OK

        return ResponseEntity.status(status).body(ScheduleUpdateResponse.of(result.schedule))
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{scheduleId}/alarm")
    override fun switchAlarm(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: AlarmSwitchRequest,
    ): AlarmSwitchResponse {
        val schedule = scheduleCommandFacade.switchAlarm(memberId, scheduleId, request.isEnabled)

        return AlarmSwitchResponse.from(schedule)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable scheduleId: Long,
    ) {
        scheduleCommandFacade.deleteSchedule(memberId, scheduleId)
    }
}
```

**Step 13: PlaceFacade에 Command 패턴 적용**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/command/SavePlaceHistoryCommand.kt`:
```kotlin
package com.dh.ondot.schedule.application.command

data class SavePlaceHistoryCommand(
    val title: String,
    val roadAddress: String,
    val longitude: Double,
    val latitude: Double,
)
```

`PlaceFacade.kt`의 `saveHistory` 메서드 변경:

Before:
```kotlin
fun saveHistory(
    memberId: Long, title: String,
    roadAddr: String, longitude: Double, latitude: Double,
) {
    placeHistoryService.record(
        memberId, title, roadAddr,
        longitude, latitude,
    )
}
```

After:
```kotlin
fun saveHistory(memberId: Long, command: SavePlaceHistoryCommand) {
    placeHistoryService.record(
        memberId, command.title, command.roadAddress,
        command.longitude, command.latitude,
    )
}
```

import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.SavePlaceHistoryCommand
```

`PlaceController.kt`의 `saveHistory` 메서드 변경:

Before:
```kotlin
override fun saveHistory(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: PlaceHistorySaveRequest,
) {
    placeFacade.saveHistory(
        memberId, request.title ?: request.roadAddress,
        request.roadAddress, request.longitude, request.latitude,
    )
}
```

After:
```kotlin
override fun saveHistory(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: PlaceHistorySaveRequest,
) {
    val command = SavePlaceHistoryCommand(
        title = request.title ?: request.roadAddress,
        roadAddress = request.roadAddress,
        longitude = request.longitude,
        latitude = request.latitude,
    )
    placeFacade.saveHistory(memberId, command)
}
```

PlaceController에 import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.SavePlaceHistoryCommand
```

**Step 14: AlarmFacade에서 SetAlarmRequest의 파라미터를 Command로 정리**

새 파일 `src/main/kotlin/com/dh/ondot/schedule/application/command/GenerateAlarmCommand.kt`:
```kotlin
package com.dh.ondot.schedule.application.command

import java.time.LocalDateTime

data class GenerateAlarmCommand(
    val appointmentAt: LocalDateTime,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
)
```

`AlarmFacade.kt`의 `generateAlarmSettingByRoute` 메서드 변경:

Before:
```kotlin
fun generateAlarmSettingByRoute(
    memberId: Long, appointmentAt: LocalDateTime,
    startX: Double, startY: Double, endX: Double, endY: Double,
): Schedule {
```

After:
```kotlin
fun generateAlarmSettingByRoute(memberId: Long, command: GenerateAlarmCommand): Schedule {
    val member = memberService.getMemberIfExists(memberId)

    val estimatedTimeMin = routeService.calculateRouteTime(
        command.startLongitude, command.startLatitude,
        command.endLongitude, command.endLatitude,
    )

    return scheduleService.setupSchedule(
        member, command.appointmentAt, estimatedTimeMin,
    )
}
```

import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.GenerateAlarmCommand
```

`LocalDateTime` import 제거 (더 이상 직접 사용하지 않음).

`AlarmController.kt`의 `setAlarm` 메서드 변경:

Before:
```kotlin
override fun setAlarm(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: SetAlarmRequest,
): SettingAlarmResponse {
    val schedule = alarmFacade.generateAlarmSettingByRoute(
        memberId, request.appointmentAt,
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
    )
```

After:
```kotlin
override fun setAlarm(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: SetAlarmRequest,
): SettingAlarmResponse {
    val command = GenerateAlarmCommand(
        request.appointmentAt,
        request.startLongitude, request.startLatitude,
        request.endLongitude, request.endLatitude,
    )
    val schedule = alarmFacade.generateAlarmSettingByRoute(memberId, command)
```

AlarmController에 import 추가:
```kotlin
import com.dh.ondot.schedule.application.command.GenerateAlarmCommand
```

**Step 15: 빌드 확인**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 16: 테스트 실행**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew test`
Expected: BUILD SUCCESSFUL

**Step 17: 커밋**

```bash
git add -A
git commit -m "refactor: schedule 모듈 Command/Result 패턴 도입 및 Facade→Presentation 의존 제거 (DH-5)"
```

---

### Task 8: 전체 빌드 검증 + push

**Step 1: 전체 빌드 + 테스트**

Run: `JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home ./gradlew clean build`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 2: push**

```bash
git push origin refactor/DH-5
```

**Step 3: PR #70 title/body 업데이트**

PR body에 코드 스타일 개선 내용 추가.
