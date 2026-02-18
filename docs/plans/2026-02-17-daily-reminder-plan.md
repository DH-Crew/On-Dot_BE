# DH-30 데일리 리마인더 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 매일 KST 22:00에 내일 일정이 있는 사용자에게 FCM 푸시 알림을 전송하는 데일리 리마인더 기능 구현

**Architecture:** notification 모듈에 DeviceToken 엔티티, FCM 인프라, 스케줄러를 추가하고, member 모듈에 리마인더 on/off 설정 API를 추가. Facade → Domain Service → Repository 계층 구조를 따름.

**Tech Stack:** Kotlin, Spring Boot 3.4.4, Firebase Admin SDK, JPA, QueryDSL, Mockito-Kotlin

---

### Task 1: Firebase Admin SDK 의존성 추가

**Files:**
- Modify: `gradle.properties`
- Modify: `gradle/core.gradle`

**Step 1: gradle.properties에 Firebase 버전 추가**

`gradle.properties` 파일 맨 하단에 추가:
```properties
### Firebase ###
firebaseAdminVersion=9.4.2
```

**Step 2: core.gradle에 의존성 추가**

`gradle/core.gradle`의 dependencies 블록에 추가:
```groovy
// Firebase Admin
implementation "com.google.firebase:firebase-admin:${firebaseAdminVersion}"
```

**Step 3: 빌드 확인**

Run: `./gradlew dependencies --configuration compileClasspath | grep firebase`
Expected: firebase-admin:9.4.2 출력

**Step 4: 커밋**

```bash
git add gradle.properties gradle/core.gradle
git commit -m "build: Firebase Admin SDK 의존성 추가"
```

---

### Task 2: DeviceToken 엔티티 & 리포지토리

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/notification/domain/DeviceToken.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/domain/repository/DeviceTokenRepository.kt`

**Step 1: DeviceToken 엔티티 작성**

```kotlin
package com.dh.ondot.notification.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_device_token_fcm", columnNames = ["fcm_token"])
    ]
)
class DeviceToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "fcm_token", nullable = false, length = 512)
    var fcmToken: String,

    @Column(name = "device_type", nullable = false, length = 20)
    val deviceType: String,
) : BaseTimeEntity() {

    companion object {
        fun create(memberId: Long, fcmToken: String, deviceType: String): DeviceToken =
            DeviceToken(memberId = memberId, fcmToken = fcmToken, deviceType = deviceType)
    }
}
```

**Step 2: DeviceTokenRepository 작성**

```kotlin
package com.dh.ondot.notification.domain.repository

import com.dh.ondot.notification.domain.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {
    fun findByFcmToken(fcmToken: String): DeviceToken?
    fun findAllByMemberId(memberId: Long): List<DeviceToken>
    fun findAllByMemberIdIn(memberIds: List<Long>): List<DeviceToken>
    fun deleteByFcmToken(fcmToken: String)
    fun deleteAllByMemberId(memberId: Long)
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/notification/domain/DeviceToken.kt \
        src/main/kotlin/com/dh/ondot/notification/domain/repository/DeviceTokenRepository.kt
git commit -m "feat: DeviceToken 엔티티 및 리포지토리 추가"
```

---

### Task 3: Member 엔티티에 dailyReminderEnabled 필드 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/domain/Member.kt`

**Step 1: Member 엔티티에 필드 추가**

Member 클래스의 프로퍼티 목록(deletedAt 필드 뒤)에 추가:
```kotlin
@Column(name = "daily_reminder_enabled", nullable = false, columnDefinition = "TINYINT(1)")
var dailyReminderEnabled: Boolean = true,
```

Member 클래스에 메서드 추가 (기존 메서드들 아래):
```kotlin
fun updateDailyReminderEnabled(enabled: Boolean) {
    this.dailyReminderEnabled = enabled
}
```

**Step 2: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/member/domain/Member.kt
git commit -m "feat: Member 엔티티에 dailyReminderEnabled 필드 추가"
```

---

### Task 4: DeviceTokenService 도메인 서비스

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/notification/domain/service/DeviceTokenService.kt`
- Create: `src/test/kotlin/com/dh/ondot/notification/domain/service/DeviceTokenServiceTest.kt`

**Step 1: 테스트 작성**

```kotlin
package com.dh.ondot.notification.domain.service

import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.repository.DeviceTokenRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
@DisplayName("DeviceTokenService 테스트")
class DeviceTokenServiceTest {

    @Mock
    private lateinit var deviceTokenRepository: DeviceTokenRepository

    @InjectMocks
    private lateinit var deviceTokenService: DeviceTokenService

    @Test
    @DisplayName("신규 FCM 토큰을 등록한다")
    fun registerToken_NewToken_SavesNewToken() {
        // given
        val memberId = 1L
        val fcmToken = "new-token-123"
        val deviceType = "iOS"
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(null)
        given(deviceTokenRepository.save(any<DeviceToken>())).willAnswer { it.arguments[0] }

        // when
        deviceTokenService.registerOrUpdate(memberId, fcmToken, deviceType)

        // then
        verify(deviceTokenRepository).findByFcmToken(fcmToken)
        verify(deviceTokenRepository).save(any<DeviceToken>())
    }

    @Test
    @DisplayName("기존 FCM 토큰이 존재하면 새로 저장하지 않는다")
    fun registerToken_ExistingToken_SkipsSave() {
        // given
        val memberId = 1L
        val fcmToken = "existing-token-123"
        val deviceType = "iOS"
        val existing = DeviceToken.create(memberId, fcmToken, deviceType)
        given(deviceTokenRepository.findByFcmToken(fcmToken)).willReturn(existing)

        // when
        deviceTokenService.registerOrUpdate(memberId, fcmToken, deviceType)

        // then
        verify(deviceTokenRepository).findByFcmToken(fcmToken)
        verify(deviceTokenRepository, never()).save(any<DeviceToken>())
    }

    @Test
    @DisplayName("회원의 모든 디바이스 토큰을 조회한다")
    fun findAllByMemberIds_ReturnsTokens() {
        // given
        val memberIds = listOf(1L, 2L)
        val tokens = listOf(
            DeviceToken.create(1L, "token-1", "iOS"),
            DeviceToken.create(2L, "token-2", "Android"),
        )
        given(deviceTokenRepository.findAllByMemberIdIn(memberIds)).willReturn(tokens)

        // when
        val result = deviceTokenService.findAllByMemberIds(memberIds)

        // then
        assertThat(result).hasSize(2)
        verify(deviceTokenRepository).findAllByMemberIdIn(memberIds)
    }
}
```

**Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.dh.ondot.notification.domain.service.DeviceTokenServiceTest"`
Expected: FAIL (DeviceTokenService 클래스 미존재)

**Step 3: DeviceTokenService 구현**

```kotlin
package com.dh.ondot.notification.domain.service

import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.repository.DeviceTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DeviceTokenService(
    private val deviceTokenRepository: DeviceTokenRepository,
) {

    @Transactional
    fun registerOrUpdate(memberId: Long, fcmToken: String, deviceType: String) {
        val existing = deviceTokenRepository.findByFcmToken(fcmToken)
        if (existing == null) {
            deviceTokenRepository.save(DeviceToken.create(memberId, fcmToken, deviceType))
        }
    }

    @Transactional
    fun deleteByFcmToken(fcmToken: String) {
        deviceTokenRepository.deleteByFcmToken(fcmToken)
    }

    @Transactional
    fun deleteAllByMemberId(memberId: Long) {
        deviceTokenRepository.deleteAllByMemberId(memberId)
    }

    fun findAllByMemberIds(memberIds: List<Long>): List<DeviceToken> =
        deviceTokenRepository.findAllByMemberIdIn(memberIds)

    @Transactional
    fun deleteByFcmTokens(fcmTokens: List<String>) {
        fcmTokens.forEach { deviceTokenRepository.deleteByFcmToken(it) }
    }
}
```

**Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "com.dh.ondot.notification.domain.service.DeviceTokenServiceTest"`
Expected: PASS (3 tests)

**Step 5: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/notification/domain/service/DeviceTokenService.kt \
        src/test/kotlin/com/dh/ondot/notification/domain/service/DeviceTokenServiceTest.kt
git commit -m "feat: DeviceTokenService 도메인 서비스 및 테스트 추가"
```

---

### Task 5: MemberService 및 MemberRepository에 리마인더 관련 메서드 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/domain/service/MemberService.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/domain/repository/MemberRepository.kt`
- Modify: `src/test/kotlin/com/dh/ondot/member/domain/service/MemberServiceTest.kt`

**Step 1: MemberServiceTest에 테스트 추가**

기존 테스트 파일 하단에 추가:
```kotlin
@Test
@DisplayName("회원의 데일리 리마인더 설정을 변경한다")
fun updateDailyReminderEnabled_ValidInput_UpdatesSuccessfully() {
    // given
    val memberId = 1L
    val member = Member.registerWithOauth("test@example.com", OauthProvider.KAKAO, "kakao123")
    given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

    // when
    val result = memberService.updateDailyReminderEnabled(memberId, false)

    // then
    assertThat(result.dailyReminderEnabled).isFalse()
    verify(memberRepository).findById(memberId)
}
```

**Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.dh.ondot.member.domain.service.MemberServiceTest.updateDailyReminderEnabled_ValidInput_UpdatesSuccessfully"`
Expected: FAIL (메서드 미존재)

**Step 3: MemberRepository에 쿼리 메서드 추가**

`MemberRepository.kt`에 추가:
```kotlin
fun findAllByDailyReminderEnabledTrue(): List<Member>
```

**Step 4: MemberService에 메서드 추가**

`MemberService.kt`에 추가:
```kotlin
@Transactional
fun updateDailyReminderEnabled(memberId: Long, enabled: Boolean): Member {
    val member = getMemberIfExists(memberId)
    member.updateDailyReminderEnabled(enabled)
    return member
}

fun findAllDailyReminderEnabledMembers(): List<Member> =
    memberRepository.findAllByDailyReminderEnabledTrue()
```

**Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests "com.dh.ondot.member.domain.service.MemberServiceTest"`
Expected: PASS

**Step 6: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/member/domain/service/MemberService.kt \
        src/main/kotlin/com/dh/ondot/member/domain/repository/MemberRepository.kt \
        src/test/kotlin/com/dh/ondot/member/domain/service/MemberServiceTest.kt
git commit -m "feat: MemberService에 데일리 리마인더 설정 메서드 추가"
```

---

### Task 6: MemberFacade 및 MemberController에 리마인더 토글 API 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/application/MemberFacade.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/MemberController.kt`
- Modify: `src/main/kotlin/com/dh/ondot/member/presentation/swagger/MemberSwagger.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/presentation/request/UpdateDailyReminderRequest.kt`
- Create: `src/main/kotlin/com/dh/ondot/member/presentation/response/DailyReminderResponse.kt`

**Step 1: Request DTO 작성**

```kotlin
package com.dh.ondot.member.presentation.request

import jakarta.validation.constraints.NotNull

data class UpdateDailyReminderRequest(
    @field:NotNull
    val enabled: Boolean,
)
```

**Step 2: Response DTO 작성**

```kotlin
package com.dh.ondot.member.presentation.response

data class DailyReminderResponse(
    val enabled: Boolean,
) {
    companion object {
        fun from(enabled: Boolean): DailyReminderResponse =
            DailyReminderResponse(enabled = enabled)
    }
}
```

**Step 3: MemberFacade에 메서드 추가**

`MemberFacade.kt`에 추가:
```kotlin
@Transactional
fun updateDailyReminderEnabled(memberId: Long, enabled: Boolean): Member =
    memberService.updateDailyReminderEnabled(memberId, enabled)
```

**Step 4: MemberSwagger에 API 문서 추가**

`MemberSwagger.kt`에 메서드 추가 (기존 메서드 아래):
```kotlin
/*──────────────────────────────────────────────────────
 * 데일리 리마인더 설정 조회
 *──────────────────────────────────────────────────────*/
@Operation(
    summary = "데일리 리마인더 설정 조회",
    description = "로그인한 회원의 데일리 리마인더(매일 저녁 10시 내일 일정 알림) on/off 설정을 조회합니다.",
    responses = [
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(
                schema = Schema(implementation = DailyReminderResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = """{ "enabled": true }"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "NOT_FOUND_MEMBER",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    value = """
                    {
                      "errorCode": "NOT_FOUND_MEMBER",
                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                    }"""
                )]
            )]
        ),
    ]
)
@GetMapping("/daily-reminder")
fun getDailyReminder(@RequestAttribute("memberId") memberId: Long): DailyReminderResponse

/*──────────────────────────────────────────────────────
 * 데일리 리마인더 설정 변경
 *──────────────────────────────────────────────────────*/
@Operation(
    summary = "데일리 리마인더 on/off 변경",
    description = "매일 저녁 10시 내일 일정 푸시 알림의 수신 여부를 변경합니다.",
    requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [Content(
            schema = Schema(implementation = UpdateDailyReminderRequest::class),
            examples = [ExampleObject(value = """{ "enabled": false }""")]
        )]
    ),
    responses = [
        ApiResponse(
            responseCode = "200",
            description = "변경 성공",
            content = [Content(
                schema = Schema(implementation = DailyReminderResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = """{ "enabled": false }"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "404",
            description = "NOT_FOUND_MEMBER",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    value = """
                    {
                      "errorCode": "NOT_FOUND_MEMBER",
                      "message": "회원을 찾을 수 없습니다. MemberId : 42"
                    }"""
                )]
            )]
        ),
    ]
)
@PatchMapping("/daily-reminder")
fun updateDailyReminder(
    @RequestAttribute("memberId") memberId: Long,
    @RequestBody request: UpdateDailyReminderRequest,
): DailyReminderResponse
```

MemberSwagger의 import에 추가:
```kotlin
import com.dh.ondot.member.presentation.request.UpdateDailyReminderRequest
import com.dh.ondot.member.presentation.response.DailyReminderResponse
```

**Step 5: MemberController에 엔드포인트 구현**

`MemberController.kt`에 추가:
```kotlin
@ResponseStatus(HttpStatus.OK)
@GetMapping("/daily-reminder")
override fun getDailyReminder(@RequestAttribute("memberId") memberId: Long): DailyReminderResponse {
    val member = memberFacade.getMember(memberId)
    return DailyReminderResponse.from(member.dailyReminderEnabled)
}

@ResponseStatus(HttpStatus.OK)
@PatchMapping("/daily-reminder")
override fun updateDailyReminder(
    @RequestAttribute("memberId") memberId: Long,
    @Valid @RequestBody request: UpdateDailyReminderRequest,
): DailyReminderResponse {
    val member = memberFacade.updateDailyReminderEnabled(memberId, request.enabled)
    return DailyReminderResponse.from(member.dailyReminderEnabled)
}
```

**Step 6: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 7: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/member/application/MemberFacade.kt \
        src/main/kotlin/com/dh/ondot/member/presentation/MemberController.kt \
        src/main/kotlin/com/dh/ondot/member/presentation/swagger/MemberSwagger.kt \
        src/main/kotlin/com/dh/ondot/member/presentation/request/UpdateDailyReminderRequest.kt \
        src/main/kotlin/com/dh/ondot/member/presentation/response/DailyReminderResponse.kt
git commit -m "feat: 데일리 리마인더 on/off 조회/변경 API 추가"
```

---

### Task 7: DeviceTokenFacade 및 DeviceTokenController

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/notification/application/DeviceTokenFacade.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/application/dto/RegisterDeviceTokenCommand.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/presentation/DeviceTokenController.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/presentation/request/RegisterDeviceTokenRequest.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/presentation/request/DeleteDeviceTokenRequest.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/presentation/swagger/DeviceTokenSwagger.kt`

**Step 1: Command DTO 작성**

```kotlin
package com.dh.ondot.notification.application.dto

data class RegisterDeviceTokenCommand(
    val memberId: Long,
    val fcmToken: String,
    val deviceType: String,
)
```

**Step 2: DeviceTokenFacade 작성**

```kotlin
package com.dh.ondot.notification.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.application.dto.RegisterDeviceTokenCommand
import com.dh.ondot.notification.domain.service.DeviceTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceTokenFacade(
    private val memberService: MemberService,
    private val deviceTokenService: DeviceTokenService,
) {

    @Transactional
    fun registerToken(command: RegisterDeviceTokenCommand) {
        memberService.getMemberIfExists(command.memberId)
        deviceTokenService.registerOrUpdate(command.memberId, command.fcmToken, command.deviceType)
    }

    @Transactional
    fun deleteToken(fcmToken: String) {
        deviceTokenService.deleteByFcmToken(fcmToken)
    }
}
```

**Step 3: Request DTO 작성**

`RegisterDeviceTokenRequest.kt`:
```kotlin
package com.dh.ondot.notification.presentation.request

import com.dh.ondot.notification.application.dto.RegisterDeviceTokenCommand
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceTokenRequest(
    @field:NotBlank
    val fcmToken: String,

    @field:NotBlank
    val deviceType: String,
) {
    fun toCommand(memberId: Long): RegisterDeviceTokenCommand =
        RegisterDeviceTokenCommand(
            memberId = memberId,
            fcmToken = fcmToken,
            deviceType = deviceType,
        )
}
```

`DeleteDeviceTokenRequest.kt`:
```kotlin
package com.dh.ondot.notification.presentation.request

import jakarta.validation.constraints.NotBlank

data class DeleteDeviceTokenRequest(
    @field:NotBlank
    val fcmToken: String,
)
```

**Step 4: Swagger 인터페이스 작성**

```kotlin
package com.dh.ondot.notification.presentation.swagger

import com.dh.ondot.core.ErrorResponse
import com.dh.ondot.notification.presentation.request.DeleteDeviceTokenRequest
import com.dh.ondot.notification.presentation.request.RegisterDeviceTokenRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(
    name = "Device Token API",
    description = """
                <b>AccessToken (Authorization: Bearer JWT)</b>은 필수입니다.<br><br>
                FCM 디바이스 토큰 등록/삭제 API.<br>
                앱 실행 시 토큰 등록, 로그아웃 시 토큰 삭제를 수행합니다.
                """
)
@RequestMapping("/device-tokens")
interface DeviceTokenSwagger {

    @Operation(
        summary = "FCM 디바이스 토큰 등록",
        description = "앱 실행 시 FCM 토큰을 서버에 등록합니다. 이미 등록된 토큰이면 무시합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = RegisterDeviceTokenRequest::class),
                examples = [ExampleObject(
                    value = """
                    {
                      "fcmToken": "dGVzdC10b2tlbi0xMjM0NTY...",
                      "deviceType": "iOS"
                    }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "등록 완료"),
            ApiResponse(
                responseCode = "404",
                description = "NOT_FOUND_MEMBER",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        value = """
                        {
                          "errorCode": "NOT_FOUND_MEMBER",
                          "message": "회원을 찾을 수 없습니다. MemberId : 42"
                        }"""
                    )]
                )]
            ),
        ]
    )
    @PostMapping
    fun registerToken(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: RegisterDeviceTokenRequest,
    )

    @Operation(
        summary = "FCM 디바이스 토큰 삭제",
        description = "로그아웃 시 FCM 토큰을 서버에서 삭제합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = DeleteDeviceTokenRequest::class),
                examples = [ExampleObject(
                    value = """{ "fcmToken": "dGVzdC10b2tlbi0xMjM0NTY..." }"""
                )]
            )]
        ),
        responses = [
            ApiResponse(responseCode = "204", description = "삭제 완료"),
        ]
    )
    @DeleteMapping
    fun deleteToken(
        @RequestAttribute("memberId") memberId: Long,
        @RequestBody request: DeleteDeviceTokenRequest,
    )
}
```

**Step 5: DeviceTokenController 작성**

```kotlin
package com.dh.ondot.notification.presentation

import com.dh.ondot.notification.application.DeviceTokenFacade
import com.dh.ondot.notification.presentation.request.DeleteDeviceTokenRequest
import com.dh.ondot.notification.presentation.request.RegisterDeviceTokenRequest
import com.dh.ondot.notification.presentation.swagger.DeviceTokenSwagger
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device-tokens")
class DeviceTokenController(
    private val deviceTokenFacade: DeviceTokenFacade,
) : DeviceTokenSwagger {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    override fun registerToken(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: RegisterDeviceTokenRequest,
    ) {
        deviceTokenFacade.registerToken(request.toCommand(memberId))
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    override fun deleteToken(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: DeleteDeviceTokenRequest,
    ) {
        deviceTokenFacade.deleteToken(request.fcmToken)
    }
}
```

**Step 6: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 7: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/notification/application/DeviceTokenFacade.kt \
        src/main/kotlin/com/dh/ondot/notification/application/dto/RegisterDeviceTokenCommand.kt \
        src/main/kotlin/com/dh/ondot/notification/presentation/DeviceTokenController.kt \
        src/main/kotlin/com/dh/ondot/notification/presentation/request/RegisterDeviceTokenRequest.kt \
        src/main/kotlin/com/dh/ondot/notification/presentation/request/DeleteDeviceTokenRequest.kt \
        src/main/kotlin/com/dh/ondot/notification/presentation/swagger/DeviceTokenSwagger.kt
git commit -m "feat: 디바이스 토큰 등록/삭제 API 추가"
```

---

### Task 8: FCM 인프라 (FcmConfig + FcmClient)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/notification/infra/fcm/FcmConfig.kt`
- Create: `src/main/kotlin/com/dh/ondot/notification/infra/fcm/FcmClient.kt`

**Step 1: FcmConfig 작성**

```kotlin
package com.dh.ondot.notification.infra.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FcmConfig(
    @Value("\${fcm.service-account-file:firebase/service-account.json}")
    private val serviceAccountFile: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isNotEmpty()) {
            log.info("FirebaseApp already initialized")
            return
        }
        val resource = ClassPathResource(serviceAccountFile)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
            .build()
        FirebaseApp.initializeApp(options)
        log.info("FirebaseApp initialized with service account: {}", serviceAccountFile)
    }
}
```

**Step 2: FcmClient 작성**

```kotlin
package com.dh.ondot.notification.infra.fcm

import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmClient {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendToTokens(tokens: List<String>, title: String, body: String): List<String> {
        if (tokens.isEmpty()) return emptyList()

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .addAllTokens(tokens)
            .build()

        val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)

        val invalidTokens = mutableListOf<String>()
        response.responses.forEachIndexed { index, sendResponse ->
            if (!sendResponse.isSuccessful) {
                val error = sendResponse.exception
                log.warn("FCM send failed for token[{}]: {}", index, error?.messagingErrorCode)
                if (error?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    invalidTokens.add(tokens[index])
                }
            }
        }

        log.info(
            "FCM multicast result: success={}, failure={}",
            response.successCount,
            response.failureCount
        )
        return invalidTokens
    }
}
```

**Step 3: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/notification/infra/fcm/FcmConfig.kt \
        src/main/kotlin/com/dh/ondot/notification/infra/fcm/FcmClient.kt
git commit -m "feat: FCM 설정 및 전송 클라이언트 추가"
```

---

### Task 9: DailyReminderScheduler 구현

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/notification/application/DailyReminderScheduler.kt`
- Create: `src/test/kotlin/com/dh/ondot/notification/application/DailyReminderSchedulerTest.kt`

**Step 1: 테스트 작성**

```kotlin
package com.dh.ondot.notification.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.domain.DeviceToken
import com.dh.ondot.notification.domain.service.DeviceTokenService
import com.dh.ondot.notification.infra.fcm.FcmClient
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@DisplayName("DailyReminderScheduler 테스트")
class DailyReminderSchedulerTest {

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @Mock
    private lateinit var deviceTokenService: DeviceTokenService

    @Mock
    private lateinit var fcmClient: FcmClient

    @InjectMocks
    private lateinit var scheduler: DailyReminderScheduler

    @Test
    @DisplayName("내일 일정이 있는 회원에게 리마인더를 전송한다")
    fun sendDailyReminder_MembersWithSchedules_SendsPush() {
        // given
        val member = createMember(1L)
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(listOf(member))

        // 단발성 일정 1개
        given(scheduleRepository.findAllByMemberIdInAndAppointmentAtBetween(
            eq(listOf(1L)), any(), any()
        )).willReturn(listOf(createSchedule(1L)))

        // 반복 일정 없음
        given(scheduleRepository.findAllByMemberIdInAndIsRepeatTrue(
            eq(listOf(1L))
        )).willReturn(emptyList())

        val token = DeviceToken.create(1L, "token-abc", "iOS")
        given(deviceTokenService.findAllByMemberIds(listOf(1L))).willReturn(listOf(token))
        given(fcmClient.sendToTokens(any(), any(), any())).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(fcmClient).sendToTokens(
            eq(listOf("token-abc")),
            any(),
            eq("내일 1개의 일정이 예정되어 있어요")
        )
    }

    @Test
    @DisplayName("내일 일정이 없으면 푸시를 보내지 않는다")
    fun sendDailyReminder_NoSchedules_SkipsPush() {
        // given
        val member = createMember(1L)
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(listOf(member))
        given(scheduleRepository.findAllByMemberIdInAndAppointmentAtBetween(
            eq(listOf(1L)), any(), any()
        )).willReturn(emptyList())
        given(scheduleRepository.findAllByMemberIdInAndIsRepeatTrue(
            eq(listOf(1L))
        )).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(fcmClient, never()).sendToTokens(any(), any(), any())
    }

    @Test
    @DisplayName("리마인더 활성 회원이 없으면 아무 작업도 하지 않는다")
    fun sendDailyReminder_NoEnabledMembers_DoesNothing() {
        // given
        given(memberService.findAllDailyReminderEnabledMembers()).willReturn(emptyList())

        // when
        scheduler.sendDailyReminder()

        // then
        verify(scheduleRepository, never()).findAllByMemberIdInAndAppointmentAtBetween(any(), any(), any())
        verify(fcmClient, never()).sendToTokens(any(), any(), any())
    }

    private fun createMember(id: Long): Member {
        val member = Member.registerWithOauth("test$id@example.com", OauthProvider.KAKAO, "kakao$id")
        // id를 리플렉션으로 설정
        val idField = Member::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(member, id)
        return member
    }

    private fun createSchedule(memberId: Long): Schedule {
        val tomorrow = TimeUtils.nowSeoulDate().plusDays(1)
        val appointmentAt = TimeUtils.toInstant(tomorrow.atTime(10, 0))
        return Schedule(memberId = memberId, appointmentAt = appointmentAt)
    }
}
```

**Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.dh.ondot.notification.application.DailyReminderSchedulerTest"`
Expected: FAIL (DailyReminderScheduler 미존재)

**Step 3: ScheduleRepository에 쿼리 메서드 추가**

`src/main/kotlin/com/dh/ondot/schedule/domain/repository/ScheduleRepository.kt`에 추가:
```kotlin
fun findAllByMemberIdInAndAppointmentAtBetween(
    memberIds: List<Long>,
    start: Instant,
    end: Instant,
): List<Schedule>

fun findAllByMemberIdInAndIsRepeatTrue(memberIds: List<Long>): List<Schedule>
```

import 추가:
```kotlin
import java.time.Instant
```

**Step 4: DailyReminderScheduler 구현**

```kotlin
package com.dh.ondot.notification.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.notification.domain.service.DeviceTokenService
import com.dh.ondot.notification.infra.fcm.FcmClient
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DailyReminderScheduler(
    private val memberService: MemberService,
    private val scheduleRepository: ScheduleRepository,
    private val deviceTokenService: DeviceTokenService,
    private val fcmClient: FcmClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val EVERY_DAY_10PM_KST = "0 0 22 * * *"
        private const val PUSH_TITLE = "온닷"
    }

    @Scheduled(cron = EVERY_DAY_10PM_KST, zone = "Asia/Seoul")
    fun sendDailyReminder() {
        log.info("Daily reminder scheduler started")

        val enabledMembers = memberService.findAllDailyReminderEnabledMembers()
        if (enabledMembers.isEmpty()) {
            log.info("No members with daily reminder enabled")
            return
        }

        val memberIds = enabledMembers.map { it.id }
        val tomorrow = TimeUtils.nowSeoulDate().plusDays(1)

        val scheduleCountByMember = countSchedulesForDate(memberIds, tomorrow)
        if (scheduleCountByMember.isEmpty()) {
            log.info("No schedules found for tomorrow ({})", tomorrow)
            return
        }

        val memberIdsWithSchedules = scheduleCountByMember.keys.toList()
        val tokens = deviceTokenService.findAllByMemberIds(memberIdsWithSchedules)
        val tokensByMember = tokens.groupBy { it.memberId }

        var totalSent = 0
        for ((memberId, count) in scheduleCountByMember) {
            val memberTokens = tokensByMember[memberId] ?: continue
            val fcmTokens = memberTokens.map { it.fcmToken }
            val body = "내일 ${count}개의 일정이 예정되어 있어요"

            val invalidTokens = fcmClient.sendToTokens(fcmTokens, PUSH_TITLE, body)
            if (invalidTokens.isNotEmpty()) {
                deviceTokenService.deleteByFcmTokens(invalidTokens)
                log.info("Deleted {} invalid FCM tokens", invalidTokens.size)
            }
            totalSent += fcmTokens.size
        }

        log.info("Daily reminder completed: {} members, {} tokens", scheduleCountByMember.size, totalSent)
    }

    private fun countSchedulesForDate(memberIds: List<Long>, date: LocalDate): Map<Long, Int> {
        val startOfDay = TimeUtils.toInstant(date.atStartOfDay())
        val endOfDay = TimeUtils.toInstant(date.plusDays(1).atStartOfDay())

        // 단발성 일정
        val singleSchedules = scheduleRepository
            .findAllByMemberIdInAndAppointmentAtBetween(memberIds, startOfDay, endOfDay)

        // 반복 일정
        val repeatSchedules = scheduleRepository
            .findAllByMemberIdInAndIsRepeatTrue(memberIds)
            .filter { isScheduledForDate(it, date) }

        val allSchedules = singleSchedules + repeatSchedules
        return allSchedules.groupBy { it.memberId }.mapValues { it.value.size }
    }

    private fun isScheduledForDate(schedule: Schedule, date: LocalDate): Boolean {
        val dayValue = (date.dayOfWeek.value % 7) + 1
        return schedule.repeatDays?.contains(dayValue) == true
    }
}
```

**Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests "com.dh.ondot.notification.application.DailyReminderSchedulerTest"`
Expected: PASS (3 tests)

**Step 6: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/notification/application/DailyReminderScheduler.kt \
        src/test/kotlin/com/dh/ondot/notification/application/DailyReminderSchedulerTest.kt \
        src/main/kotlin/com/dh/ondot/schedule/domain/repository/ScheduleRepository.kt
git commit -m "feat: 데일리 리마인더 스케줄러 구현 및 테스트 추가"
```

---

### Task 10: 회원 탈퇴 시 디바이스 토큰 정리

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/member/application/MemberFacade.kt`

**Step 1: MemberFacade에 DeviceTokenService 주입 및 탈퇴 로직 수정**

`MemberFacade`의 생성자에 `DeviceTokenService` 추가:
```kotlin
private val deviceTokenService: DeviceTokenService,
```

import 추가:
```kotlin
import com.dh.ondot.notification.domain.service.DeviceTokenService
```

`deleteMember` 메서드에서 기존 삭제 로직(scheduleService, addressService 등)과 함께 추가:
```kotlin
deviceTokenService.deleteAllByMemberId(memberId)
```

**Step 2: 빌드 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/member/application/MemberFacade.kt
git commit -m "feat: 회원 탈퇴 시 디바이스 토큰 삭제 추가"
```

---

### Task 11: 전체 테스트 및 빌드 검증

**Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

**Step 2: 애플리케이션 빌드 확인**

Run: `./gradlew build -x test`
Expected: BUILD SUCCESSFUL

**Step 3: 빌드 경고 확인**

Run: `./gradlew build 2>&1 | grep -i "warning\|deprecated"`
Expected: 새로운 경고 없음 (기존 경고만)

---

### 구현 후 프론트엔드 전달사항

Firebase 서비스 계정 키 파일(`src/main/resources/firebase/service-account.json`)은 별도로 설정 필요. 프론트엔드 팀에 전달할 내용:

1. **앱 실행/로그인 시**: `POST /device-tokens` 호출 (fcmToken, deviceType)
2. **로그아웃 시**: `DELETE /device-tokens` 호출 (fcmToken)
3. **FCM 토큰 갱신 콜백**: `POST /device-tokens` 재호출
4. **설정 화면**: `GET /members/daily-reminder` 조회, `PATCH /members/daily-reminder` 토글
5. **푸시 수신 처리**: 앱에서 FCM 알림 수신 UI 구현
