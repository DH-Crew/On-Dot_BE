# DH-5: Full Kotlin Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** on-dot 프로젝트의 전체 Java 소스(297 main + 16 test)를 idiomatic Kotlin으로 전환한다. 운영 중인 코드이므로 비즈니스 플로우를 100% 보존해야 한다.

**Architecture:** Phase별 순차 전환 (core → member → notification+schedule 병렬 → cleanup). 각 파일은 원본 Java를 읽고, baro 프로젝트 패턴을 참고하여 1:1 대응 Kotlin으로 재작성한다. 레이어 완료마다 빌드 검증.

**Tech Stack:** Kotlin 1.9.22, Spring Boot 3.4.4, Gradle (Groovy DSL), kapt, allOpen/noArg plugins

**Reference project:** `/Users/hs/IdeaProjects/baro` — 동일 팀의 Kotlin Spring Boot 프로젝트

**Verify command:** `export JAVA_HOME=/Users/hs/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home && ./gradlew clean build`

---

## 비즈니스 플로우 보존 규칙 (CRITICAL)

모든 전환 작업에서 반드시 준수:

1. **메서드 시그니처 보존**: 모든 public/protected 메서드의 이름, 파라미터 타입, 반환 타입을 동일하게 유지
2. **예외 동작 보존**: 예외 발생 조건, 예외 타입, 에러 메시지 포맷을 변경하지 않음
3. **Spring annotation 보존**: `@Transactional`, `@Async`, `@EventListener`, `@Scheduled` 등 동일 적용
4. **JPA mapping 보존**: 컬럼명, 테이블명, 관계 매핑, fetch 전략, cascade 변경 없음
5. **Java interop 보장**: 아직 Java인 모듈에서 Kotlin 클래스를 참조할 때 `@JvmStatic`, `@JvmField` 등 필요한 어노테이션 추가
6. **검증**: 각 레이어 전환 완료 후 `./gradlew clean build` 통과 필수

---

## Kotlin 전환 패턴 가이드 (baro 기준)

### Entity
```kotlin
// Java: @Getter @Builder @NoArgsConstructor(PROTECTED) @AllArgsConstructor(PRIVATE) @Entity
// Kotlin:
@Entity
@Table(name = "table_name")
class EntityName(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "col_name")
    var fieldName: String,
) : BaseTimeEntity() {

    // 도메인 로직 메서드 — 원본과 동일한 비즈니스 로직
    fun doSomething() { ... }

    companion object {
        fun create(...): EntityName = EntityName(...)
    }
}
```

### Service
```kotlin
// Java: @Service @RequiredArgsConstructor @Transactional(readOnly=true)
// Kotlin:
@Service
@Transactional(readOnly = true)
class SomeService(
    private val repository: SomeRepository,
) {
    @Transactional
    fun createSomething(cmd: Command): Something {
        // 원본과 동일한 비즈니스 로직
    }
}
```

### Record → data class
```kotlin
// Java: public record FooRequest(String name, int age) {}
// Kotlin:
data class FooRequest(
    val name: String,
    val age: Int,
)
```

### Enum
```kotlin
// Java: @Getter @RequiredArgsConstructor public enum Foo { BAR("value"); private final String v; }
// Kotlin:
enum class Foo(val v: String) {
    BAR("value"),
    ;
}
```

### Logging
```kotlin
// Java: @Slf4j
// Kotlin:
private val log = LoggerFactory.getLogger(javaClass)
```

### static method
```kotlin
// Java: public static Foo from(...) { ... }
// Kotlin:
companion object {
    @JvmStatic  // Java interop 필요시에만
    fun from(...): Foo = Foo(...)
}
```

### Swagger interface
```kotlin
// Java: interface FooSwagger { ... }
// Kotlin: 그대로 interface로 유지, @Operation 등 annotation 동일
interface FooSwagger {
    @Operation(summary = "...")
    @ApiResponse(...)
    fun methodName(...): ResponseType
}
```

---

## Phase 1: core 전환 (기존 플랜)

> 이미 `docs/plans/2026-02-16-kotlin-migration-plan.md`에 Task 1-12로 작성됨.
> Task 1-3 (빌드 설정) 완료. Task 4-12 (코드 전환) 실행 중.

---

## Phase 2: member 도메인 전환 (97 main + 6 test files)

### Task 13: member/core — exceptions 전환 (14 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/core/exception/` (전체)
- Delete: `src/main/java/com/dh/ondot/member/core/AppleProperties.java`
- Delete: `src/main/java/com/dh/ondot/member/core/JwtProperties.java`
- Delete: `src/main/java/com/dh/ondot/member/core/OauthProviderConverter.java`
- Delete: `src/main/java/com/dh/ondot/member/core/TokenExtractor.java`
- Create: `src/main/kotlin/com/dh/ondot/member/core/exception/` (Kotlin 파일)
- Create: `src/main/kotlin/com/dh/ondot/member/core/` (properties, converter, extractor)

**Step 1:** 원본 Java 파일을 모두 읽는다:
```bash
find src/main/java/com/dh/ondot/member/core -name "*.java" -type f
```

**Step 2:** 각 exception 클래스를 Kotlin으로 전환한다. 모든 exception은 core의 추상 예외를 상속하며 동일 패턴:

```kotlin
// 예시: NotFoundMemberException
package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode
import com.dh.ondot.core.exception.NotFoundException

class NotFoundMemberException(memberId: Long) :
    NotFoundException(ErrorCode.NOT_FOUND_MEMBER.message.format(memberId)) {
    override val errorCode: String get() = ErrorCode.NOT_FOUND_MEMBER.name
}
```

**주의:** 각 exception의 생성자 파라미터와 ErrorCode 매핑을 원본과 정확히 일치시킨다. `format()` 인자 타입(%d vs %s)을 확인한다.

**Step 3:** `AppleProperties.kt`, `JwtProperties.kt` 전환 (ConfigurationProperties → data class)

```kotlin
@ConfigurationProperties(prefix = "apple")
data class AppleProperties(
    val teamId: String,
    val clientId: String,
    val keyId: String,
    val keyPath: String,
    val tokenUrl: String,
)
```

**Step 4:** `OauthProviderConverter.kt`, `TokenExtractor.kt` 전환

**Step 5:** Java 파일 삭제

**Step 6:** 빌드 검증
```bash
./gradlew clean build
```

**Step 7:** 커밋
```bash
git add -A
git commit -m "refactor: member/core exception 및 설정 클래스를 Kotlin으로 전환"
```

---

### Task 14: member/domain — entities, enums, VOs 전환 (약 15 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/domain/` (entities, enums, VOs, factories, DTOs)
- Create: `src/main/kotlin/com/dh/ondot/member/domain/` (대응 Kotlin 파일)

**Step 1:** 원본 Java 파일을 모두 읽는다:
```bash
find src/main/java/com/dh/ondot/member/domain -name "*.java" -type f -not -path "*/service/*" -not -path "*/repository/*"
```

**Step 2:** Entity 클래스 전환. **비즈니스 로직 보존이 가장 중요한 레이어.**

전환 규칙:
- `@Getter` → Kotlin property (자동 getter)
- `@Builder` → companion object factory method 또는 named parameters
- `@NoArgsConstructor(PROTECTED)` → JPA를 위해 allOpen 플러그인이 처리 (build.gradle에 설정됨)
- `@AllArgsConstructor(PRIVATE)` → primary constructor
- `@Embedded` → 동일하게 유지
- `@Enumerated(EnumType.STRING)` → 동일하게 유지
- `@OneToMany`, `@ManyToOne` 등 관계 매핑 → 동일하게 유지
- Entity 내부의 도메인 메서드(update, validate 등) → 동일 로직으로 전환

**Step 3:** Enum 클래스 전환 (`OauthProvider`, `AddressType`, `MapProvider`)

전환 규칙:
- Java enum의 `from()` static 메서드 → companion object `from()` 메서드
- `@JvmStatic` 추가 (Java에서 아직 호출하는 경우)

**Step 4:** Value Object 전환 (`OauthInfo`, `UserInfo`)

**Step 5:** Event 전환 (`UserRegistrationEvent`)

**Step 6:** Java 파일 삭제

**Step 7:** 빌드 검증
```bash
./gradlew clean build
```

**Step 8:** 커밋
```bash
git add -A
git commit -m "refactor: member/domain entity, enum, VO를 Kotlin으로 전환"
```

---

### Task 15: member/domain — repositories, services 전환 (약 11 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/domain/repository/` (7 files)
- Delete: `src/main/java/com/dh/ondot/member/domain/service/` (4 files)
- Create: `src/main/kotlin/com/dh/ondot/member/domain/repository/`
- Create: `src/main/kotlin/com/dh/ondot/member/domain/service/`

**Step 1:** Repository 전환 — JpaRepository interface는 거의 그대로:
```kotlin
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByOauthInfoOauthIdAndOauthInfoOauthProvider(oauthId: String, oauthProvider: OauthProvider): Member?
}
```

**Step 2:** Service 전환 — 비즈니스 로직 1:1 보존:
```kotlin
@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun getMember(memberId: Long): Member =
        memberRepository.findById(memberId)
            .orElseThrow { NotFoundMemberException(memberId) }

    @Transactional
    fun updateSomething(...) { ... }  // 원본 로직 그대로
}
```

**주의:** `@Transactional` 범위와 readOnly 설정을 원본과 정확히 일치시킨다.

**Step 3:** Java 파일 삭제

**Step 4:** 빌드 검증
```bash
./gradlew clean build
```

**Step 5:** 커밋
```bash
git add -A
git commit -m "refactor: member/domain repository, service를 Kotlin으로 전환"
```

---

### Task 16: member/application — facades, commands, DTOs 전환 (9 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/application/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/member/application/`

**Step 1:** Command/DTO 전환 (record → data class):
```kotlin
data class OnboardingCommand(
    val memberId: Long,
    val answerIds: List<Long>,
    val address: CreateAddressCommand,
) {
    companion object {
        fun from(memberId: Long, request: OnboardingRequest): OnboardingCommand = ...
    }
}
```

**Step 2:** Facade 전환 — 비즈니스 플로우 보존 핵심:

```kotlin
@Service
class AuthFacade(
    private val memberService: MemberService,
    private val tokenFacade: TokenFacade,
    private val oauthApiFactory: OauthApiFactory,
    private val eventPublisher: ApplicationEventPublisher,
) {
    // 원본 메서드와 동일한 호출 순서, 예외 처리, 이벤트 발행
}
```

**Step 3:** `TokenFacade`, `TokenManager` 전환

**Step 4:** Java 파일 삭제

**Step 5:** 빌드 검증
```bash
./gradlew clean build
```

**Step 6:** 커밋
```bash
git add -A
git commit -m "refactor: member/application facade, command, DTO를 Kotlin으로 전환"
```

---

### Task 17: member/api — controllers, requests, responses, swagger 전환 (22 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/api/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/member/api/`

**Step 1:** Request/Response 전환 (record → data class)

**Step 2:** Swagger interface 전환

**Step 3:** Controller 전환 — endpoint path, HTTP method, status code, 파라미터 바인딩을 원본과 정확히 일치

```kotlin
@RestController
@RequestMapping("/members")
class MemberController(
    private val memberFacade: MemberFacade,
) : MemberSwagger {

    @GetMapping("/preparation-time")
    override fun getPreparationTime(
        @RequestAttribute memberId: Long,
    ): PreparationTimeResponse {
        return memberFacade.getPreparationTime(memberId)
    }
}
```

**Step 4:** Java 파일 삭제

**Step 5:** 빌드 검증
```bash
./gradlew clean build
```

**Step 6:** 커밋
```bash
git add -A
git commit -m "refactor: member/api controller, request, response를 Kotlin으로 전환"
```

---

### Task 18: member/infra — OAuth, JWT, Redis 전환 (22 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/member/infra/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/member/infra/`

**Step 1:** OAuth API 구현체 전환 (`AppleOauthApi`, `KakaoOauthApi`, `OauthApiFactoryImpl`)

**주의:** 외부 API 호출 로직, 에러 핸들링, 토큰 파싱을 원본과 정확히 일치시킨다.

**Step 2:** JWT 유틸 전환 (`AppleJwtUtil`)

**Step 3:** Redis 전환 (`RedisTokenRepository`)

**Step 4:** DTO 전환 (`ApplePublicKeyResponse`, `KakaoUserInfoResponse` 등)

**Step 5:** Java 파일 삭제

**Step 6:** 빌드 검증
```bash
./gradlew clean build
```

**Step 7:** 커밋
```bash
git add -A
git commit -m "refactor: member/infra OAuth, JWT, Redis를 Kotlin으로 전환"
```

---

### Task 19: member tests 전환 (6 files)

**Files:**
- Delete: `src/test/java/com/dh/ondot/member/` (전체)
- Create: `src/test/kotlin/com/dh/ondot/member/`

**Step 1:** Fixture 전환 (`MemberFixture`)

**Step 2:** Service 테스트 전환 (`AddressServiceTest`, `ChoiceServiceTest`, `MemberServiceTest`, `WithdrawalServiceTest`)

**Step 3:** Java 파일 삭제 + Java member 디렉토리 삭제:
```bash
rm -rf src/main/java/com/dh/ondot/member
rm -rf src/test/java/com/dh/ondot/member
```

**Step 4:** 빌드 + 테스트 검증
```bash
./gradlew clean build
```

**Step 5:** 커밋
```bash
git add -A
git commit -m "refactor: member 테스트를 Kotlin으로 전환, Java member 디렉토리 삭제"
```

---

## Phase 3A: notification 도메인 전환 (21 files, 병렬 Agent 1)

> **병렬 실행:** Phase 3A와 3B는 동시에 실행된다. notification과 schedule은 서로 의존하지 않는다.

### Task 20: notification/domain 전환 (11 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/notification/domain/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/notification/domain/`

**Step 1:** Entity 전환 (`EmergencyAlert`, `SubwayAlert`)

```kotlin
@Entity
@Table(name = "emergency_alert")
class EmergencyAlert(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "region_name", nullable = false)
    val regionName: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
) {
    companion object {
        fun create(content: String, regionName: String, createdAt: LocalDateTime): EmergencyAlert =
            EmergencyAlert(
                content = content,
                regionName = regionName,
                createdAt = TimeUtils.toInstant(createdAt),
            )
    }
}
```

**Step 2:** Enum/VO 전환 (`AlertType`, `AlertIssue`)

**Step 3:** DTO 전환 (`EmergencyAlertDto`, `SubwayAlertDto` — record → data class)

**Step 4:** Repository 전환

**Step 5:** Service 전환 — 비즈니스 로직 1:1 보존

**Step 6:** Java 파일 삭제

**Step 7:** 빌드 검증
```bash
./gradlew clean build
```

**Step 8:** 커밋
```bash
git add -A
git commit -m "refactor: notification/domain을 Kotlin으로 전환"
```

---

### Task 21: notification/infra 전환 (10 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/notification/infra/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/notification/infra/`

**Step 1:** Discord 전환 (`DiscordWebhookClient`, `DiscordMessageTemplate`, `UserRegistrationEventListener`)

**주의:** `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` + `@Async` 조합을 정확히 유지

**Step 2:** Emergency alert 전환 (`EmergencyAlertApi`, `EmergencyAlertJsonExtractor`, `EmergencyAlertDtoMapper`)

**Step 3:** Subway alert 전환 (`SubwayAlertApi`, `SubwayAlertJsonExtractor`, `SubwayAlertDtoMapper`)

**Step 4:** Batch job 전환 (`PublicAlertBatchJob`)

**Step 5:** Java 파일 삭제 + Java notification 디렉토리 삭제:
```bash
rm -rf src/main/java/com/dh/ondot/notification
```

**Step 6:** 빌드 검증
```bash
./gradlew clean build
```

**Step 7:** 커밋
```bash
git add -A
git commit -m "refactor: notification/infra를 Kotlin으로 전환, Java notification 디렉토리 삭제"
```

---

## Phase 3B: schedule 도메인 전환 (145 main + 9 test files, 병렬 Agent 2)

### Task 22: schedule/core — exceptions, serialization 전환 (11 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/core/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/schedule/core/`

**Step 1:** Exception 전환 (9 files) — member/core와 동일 패턴

**Step 2:** Serialization 전환 (`EventSerializer` interface, `JacksonEventSerializer`)

**Step 3:** Java 파일 삭제

**Step 4:** 빌드 검증
```bash
./gradlew clean build
```

**Step 5:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/core exception, serialization을 Kotlin으로 전환"
```

---

### Task 23: schedule/domain — entities, enums, VOs, converters, events 전환 (약 20 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/domain/` (entities, enums, VOs, converters, events)
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/`

**Step 1:** Enum 전환 (7 files: `AlarmMode`, `AlarmTriggerAction`, `Mission`, `RingTone`, `SnoozeCount`, `SnoozeInterval`, `SoundCategory`)

**주의:** 각 enum의 `from()` 메서드가 `UnsupportedException`을 던지는 패턴 유지. Java에서 아직 호출하는 경우 `@JvmStatic` 추가.

**Step 2:** Value Object 전환 (`Snooze`, `Sound`)

**Step 3:** Converter 전환 (`RepeatDaysConverter`)

**Step 4:** Event 전환 (`QuickScheduleRequestedEvent` — record → data class)

**Step 5:** Entity 전환 (`Schedule`, `Alarm`, `Place`, `PlaceHistory`, `AiUsage`, `OdsayUsage`, `AlarmTriggerHistory`)

**비즈니스 로직 보존 핵심 체크:**
- `Schedule.createSchedule()`, `Schedule.createWithDefaultAlarmSetting()` — 팩토리 메서드의 알람 생성 로직
- `Alarm.createPreparationAlarm()`, `Alarm.createDepartureAlarm()` 등 — 팩토리 메서드
- `Schedule.update()` — 업데이트 로직의 null 체크, 알람 재생성 조건
- `Alarm.switchOn()`, `Alarm.switchOff()` — 상태 변경 로직
- `@OneToMany` cascade, orphanRemoval 설정 보존

**Step 6:** Java 파일 삭제

**Step 7:** 빌드 검증
```bash
./gradlew clean build
```

**Step 8:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/domain entity, enum, VO를 Kotlin으로 전환"
```

---

### Task 24: schedule/domain — repositories, services 전환 (14 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/domain/repository/` (6 files)
- Delete: `src/main/java/com/dh/ondot/schedule/domain/service/` (8 files)
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/repository/`
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/service/`

**Step 1:** Repository 전환

**Step 2:** Service 전환 — 비즈니스 로직 1:1 보존

**주의:**
- `ScheduleService.createSchedule()` — 알람 생성, 장소 저장 순서
- `ScheduleQueryService` — 조회 로직, 페이징
- `AlarmService` — 알람 on/off, 트리거 기록
- `RouteService` — 외부 API 호출 후 결과 가공 로직
- `AiUsageService`, `OdsayUsageService` — 일일 사용량 제한 로직

**Step 3:** Java 파일 삭제

**Step 4:** 빌드 검증
```bash
./gradlew clean build
```

**Step 5:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/domain repository, service를 Kotlin으로 전환"
```

---

### Task 25: schedule/application — facades, mappers, commands, DTOs, handlers 전환 (15 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/application/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/`

**Step 1:** Command/DTO 전환 (record → data class)

**Step 2:** MapStruct mapper 전환 (`QuickScheduleMapper`, `HomeScheduleListItemMapper`)

**주의:** MapStruct는 Kotlin에서 kapt로 처리된다. `@Mapper(componentModel = "spring")` 유지. interface로 유지.

**Step 3:** Facade 전환 — 비즈니스 플로우 보존 핵심

**주의:**
- `ScheduleCommandFacade.createSchedule()` — 트랜잭션 내 호출 순서
- `ScheduleQueryFacade` — 조회 + 알림 정보 조합 로직
- `AlarmFacade` — 알람 설정/해제 플로우
- `PlaceFacade` — 장소 검색 + 캐시 로직

**Step 4:** Handler 전환 (`QuickScheduleInternalEventHandler`)

**Step 5:** Java 파일 삭제

**Step 6:** 빌드 검증
```bash
./gradlew clean build
```

**Step 7:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/application facade, mapper, command를 Kotlin으로 전환"
```

---

### Task 26: schedule/api — controllers, requests, responses, swagger 전환 (36 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/api/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/schedule/api/`

**Step 1:** Request/Response 전환 (record → data class)

**주의:** nested record 구조 (예: `ScheduleCreateRequest` 내부의 `PlaceDto`)를 nested data class로 정확히 전환

**Step 2:** Swagger interface 전환

**Step 3:** Controller 전환 — endpoint 매핑 정확히 보존

**Step 4:** Java 파일 삭제

**Step 5:** 빌드 검증
```bash
./gradlew clean build
```

**Step 6:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/api controller, request, response를 Kotlin으로 전환"
```

---

### Task 27: schedule/infra — APIs, DTOs, events, exceptions, outbox, redis 전환 (31 files)

**Files:**
- Delete: `src/main/java/com/dh/ondot/schedule/infra/` (전체)
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/`

**Step 1:** Exception 전환 (8 Odsay exceptions)

**Step 2:** External API 전환 (`KakaoSearchRoadAddressApi`, `NaverSearchPlaceApi`, `OdsayPathApi`, `OpenAiPromptApi`)

**주의:** 외부 API 호출, 에러 핸들링, 응답 파싱 로직을 원본과 정확히 일치

**Step 3:** DTO 전환 (nested record 구조 → nested data class)

**Step 4:** Event listener 전환 (`ScheduleInternalEventListener`, `ScheduleInternalEventRecordListener`)

**Step 5:** Outbox 전환 (`OutboxMessage`, `OutboxBatchDispatcher`, `OutboxMessageHandler`, `OutboxMessageRouter`, `OutboxRetryScheduler`, `OutboxMessageRepository`, `MessageStatus`)

**주의:** Outbox 패턴의 메시지 상태 관리, 재시도 로직, 이벤트 라우팅을 정확히 보존

**Step 6:** Redis 전환 (`PlaceHistoryRedisRepository`, `PlaceHistoryJsonConverter`, `PlaceHistoryCleaner`)

**Step 7:** QueryDsl 전환 (`ScheduleQueryRepository`)

**주의:** Q-type alias, fetchJoin, Slice 페이징 로직 보존

**Step 8:** Java 파일 삭제

**Step 9:** 빌드 검증
```bash
./gradlew clean build
```

**Step 10:** 커밋
```bash
git add -A
git commit -m "refactor: schedule/infra API, outbox, redis를 Kotlin으로 전환"
```

---

### Task 28: schedule tests 전환 (9 files)

**Files:**
- Delete: `src/test/java/com/dh/ondot/schedule/` (전체)
- Create: `src/test/kotlin/com/dh/ondot/schedule/`

**Step 1:** Fixture 전환 (4 files)

**Step 2:** Service 테스트 전환 (3 files)

**Step 3:** Domain 테스트 전환 (`ScheduleTest`)

**Step 4:** Mapper 테스트 전환 (`HomeScheduleListItemMapperTest`)

**Step 5:** Repository 테스트 전환 (`ScheduleQueryRepositoryTest`)

**Step 6:** Java 파일 삭제 + Java schedule 디렉토리 삭제:
```bash
rm -rf src/main/java/com/dh/ondot/schedule
rm -rf src/test/java/com/dh/ondot/schedule
```

**Step 7:** 빌드 + 테스트 검증
```bash
./gradlew clean build
```

**Step 8:** 커밋
```bash
git add -A
git commit -m "refactor: schedule 테스트를 Kotlin으로 전환, Java schedule 디렉토리 삭제"
```

---

## Phase 4: Cleanup

### Task 29: OnDotApplication 전환 + 최종 정리

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/OnDotApplication.java`
- Create: `src/main/kotlin/com/dh/ondot/OnDotApplication.kt`

**Step 1:** OnDotApplication.kt 전환

```kotlin
package com.dh.ondot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OnDotApplication

fun main(args: Array<String>) {
    runApplication<OnDotApplication>(*args)
}
```

**Step 2:** Lombok 임시 의존성 제거 — `gradle/core.gradle`에서:
```groovy
// 아래 4줄 삭제:
// compileOnly "org.projectlombok:lombok"
// annotationProcessor "org.projectlombok:lombok"
// kapt "org.projectlombok:lombok"
// kapt "org.projectlombok:lombok-mapstruct-binding:0.2.0"
```

**Step 3:** `build.gradle`에서 `kapt.keepJavacAnnotationProcessors = true` 블록 삭제

**Step 4:** 빈 Java 디렉토리 삭제:
```bash
rm -rf src/main/java
rm -rf src/test/java
```

**Step 5:** 최종 빌드 + 전체 테스트 검증
```bash
./gradlew clean build
```

**Step 6:** 커밋
```bash
git add -A
git commit -m "refactor: Kotlin 전환 완료 — Lombok 제거, Java 디렉토리 삭제"
```

---

## Summary

| Phase | Task | 내용 | 파일 수 |
|-------|------|------|---------|
| 1 | 1-12 | core 전환 (기존 플랜) | 32+1 |
| 2 | 13 | member/core exceptions + config | 14 |
| 2 | 14 | member/domain entities, enums, VOs | ~15 |
| 2 | 15 | member/domain repositories, services | ~11 |
| 2 | 16 | member/application facades, commands | 9 |
| 2 | 17 | member/api controllers, requests | 22 |
| 2 | 18 | member/infra OAuth, JWT, Redis | 22 |
| 2 | 19 | member tests | 6 |
| 3A | 20 | notification/domain | 11 |
| 3A | 21 | notification/infra + cleanup | 10 |
| 3B | 22 | schedule/core exceptions | 11 |
| 3B | 23 | schedule/domain entities, enums | ~20 |
| 3B | 24 | schedule/domain repos, services | 14 |
| 3B | 25 | schedule/application | 15 |
| 3B | 26 | schedule/api | 36 |
| 3B | 27 | schedule/infra | 31 |
| 3B | 28 | schedule tests | 9 |
| 4 | 29 | OnDotApplication + cleanup | 1+cleanup |
| **Total** | | | **~313 files** |

## Subagent 병렬 실행 전략

```
[Main Agent]
  ├─ Phase 1: core (Task 4-12) — 순차 실행
  ├─ Phase 2: member (Task 13-19) — 순차 실행
  ├─ Phase 3: 병렬 dispatch
  │   ├─ [Agent 1] notification (Task 20-21)
  │   └─ [Agent 2] schedule (Task 22-28)
  └─ Phase 4: cleanup (Task 29) — 양쪽 완료 후 실행
```

Phase 3에서 병렬 실행 시 주의:
- 두 agent는 서로 다른 도메인 디렉토리에서만 작업 (충돌 없음)
- `build.gradle`, `gradle/*.gradle` 파일은 수정하지 않음
- 각 agent가 독립적으로 `./gradlew clean build` 검증
- 두 agent 모두 완료 후 Phase 4 진행
