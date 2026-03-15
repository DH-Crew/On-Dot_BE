# DH-21: 캘린더 기능 구현 계획

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Schedule을 소프트 딜리트로 전환하고, 캘린더 범위/일별 조회 API + 기록 삭제 API를 구현한다.

**Architecture:** 기존 schedule 모듈의 레이어드 아키텍처(domain → application → presentation)를 따른다. 캘린더 조회는 별도 CalendarQueryFacade에서 Schedule 테이블의 쿼리 타임 계산으로 과거/미래를 도출한다. CalendarRecordExclusion은 독립 엔티티로 관리한다.

**Tech Stack:** Kotlin, Spring Boot, JPA/Hibernate, QueryDSL, JUnit 5 + Mockito + AssertJ

**Spec:** `docs/superpowers/specs/2026-03-15-calendar-feature-design.md`

---

## File Structure

### 신규 생성

| File | Responsibility |
|------|---------------|
| `schedule/domain/CalendarRecordExclusion.kt` | 캘린더 기록 제외 엔티티 |
| `schedule/domain/repository/CalendarRecordExclusionRepository.kt` | JPA 리포지토리 |
| `schedule/domain/service/CalendarRecordExclusionService.kt` | 제외 기록 도메인 서비스 |
| `schedule/domain/enums/CalendarScheduleType.kt` | ALARM/RECORD 타입 enum |
| `schedule/application/CalendarQueryFacade.kt` | 캘린더 조회 오케스트레이션 |
| `schedule/application/CalendarCommandFacade.kt` | 캘린더 기록 삭제 오케스트레이션 |
| `schedule/application/dto/CalendarDayItem.kt` | 월별 조회 날짜별 DTO |
| `schedule/application/dto/CalendarScheduleItem.kt` | 월별 조회 스케줄 DTO |
| `schedule/application/dto/CalendarDailyItem.kt` | 일별 조회 스케줄 DTO |
| `schedule/infra/CalendarQueryRepository.kt` | 캘린더 전용 QueryDSL 리포지토리 |
| `schedule/presentation/CalendarController.kt` | 캘린더 REST 컨트롤러 |
| `schedule/presentation/swagger/CalendarSwagger.kt` | Swagger 인터페이스 |
| `schedule/presentation/response/CalendarRangeResponse.kt` | 범위 조회 응답 DTO |
| `schedule/presentation/response/CalendarDailyResponse.kt` | 일별 조회 응답 DTO |
| `schedule/core/exception/InvalidCalendarDateRangeException.kt` | 날짜 범위 검증 예외 |
| `test/.../fixture/CalendarRecordExclusionFixture.kt` | 테스트 픽스처 |
| `test/.../domain/CalendarRecordExclusionTest.kt` | 엔티티 테스트 |
| `test/.../domain/service/CalendarRecordExclusionServiceTest.kt` | 서비스 테스트 |
| `test/.../application/CalendarQueryFacadeTest.kt` | 퍼사드 테스트 |
| `test/.../application/CalendarCommandFacadeTest.kt` | 커맨드 퍼사드 테스트 |

### 수정 대상

| File | Change |
|------|--------|
| `schedule/domain/Schedule.kt` | `deletedAt` 필드 추가, `softDelete()` 메서드 추가, `isScheduledForDayOfWeek()` public으로 변경 |
| `schedule/domain/service/ScheduleService.kt` | `deleteSchedule()` → 소프트 딜리트, `deleteAllByMemberId()` → 소프트 딜리트 |
| `schedule/domain/service/ScheduleQueryService.kt` | `findScheduleById()`, `findScheduleByMemberIdAndId()` → `deletedAt` 검증 |
| `schedule/domain/repository/ScheduleRepository.kt` | 쿼리에 `deletedAt IS NULL` 조건 추가 |
| `schedule/infra/ScheduleQueryRepository.kt` | QueryDSL 쿼리에 `deletedAt` 조건 추가 |
| `core/exception/ErrorCode.kt` | 캘린더 관련 에러코드 추가 |
| `test/.../fixture/ScheduleFixture.kt` | `deletedAt` 설정 빌더 메서드 추가 |
| `test/.../domain/service/ScheduleServiceTest.kt` | 소프트 딜리트 테스트 추가 |

---

## Chunk 1: 소프트 딜리트 전환

### Task 1: Schedule 엔티티에 deletedAt 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/Schedule.kt`
- Test: `src/test/kotlin/com/dh/ondot/schedule/domain/ScheduleTest.kt`

- [ ] **Step 1: Schedule 엔티티에 deletedAt 필드와 softDelete() 메서드 추가**

```kotlin
// Schedule.kt - 생성자 파라미터에 추가 (transportType 뒤)
@Column(name = "deleted_at")
var deletedAt: Instant? = null,

// 클래스 본문에 메서드 추가
fun softDelete() {
    this.deletedAt = Instant.now()
}

fun isDeleted(): Boolean = deletedAt != null
```

- [ ] **Step 2: isScheduledForDayOfWeek 메서드를 public으로 변경**

```kotlin
// Schedule.kt - private -> 접근제한자 제거 (캘린더 조회에서 사용)
fun isScheduledForDayOfWeek(date: LocalDate): Boolean {
    val dayValue = (date.dayOfWeek.value % 7) + 1
    return repeatDays?.contains(dayValue) ?: false
}
```

> `repeatDays!!`에서 `repeatDays?.contains(dayValue) ?: false`로 null-safe 변경 (코드 스타일 규칙: `!!` 사용 금지)

- [ ] **Step 3: ScheduleTest에 softDelete 테스트 추가**

```kotlin
// ScheduleTest.kt
@Nested
@DisplayName("softDelete 테스트")
inner class SoftDeleteTest {
    @Test
    @DisplayName("softDelete 호출 시 deletedAt이 설정된다")
    fun softDelete_SetsDeletedAt() {
        val schedule = ScheduleFixture.defaultSchedule()
        assertThat(schedule.deletedAt).isNull()

        schedule.softDelete()

        assertThat(schedule.deletedAt).isNotNull()
        assertThat(schedule.isDeleted()).isTrue()
    }
}
```

- [ ] **Step 4: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.domain.ScheduleTest"`
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/domain/Schedule.kt src/test/kotlin/com/dh/ondot/schedule/domain/ScheduleTest.kt
git commit -m "feat: Schedule 엔티티에 deletedAt 필드 및 softDelete 메서드 추가"
```

---

### Task 2: ScheduleFixture에 deletedAt 빌더 추가

**Files:**
- Modify: `src/test/kotlin/com/dh/ondot/schedule/fixture/ScheduleFixture.kt`

- [ ] **Step 1: ScheduleBuilder에 deletedAt 지원 추가**

```kotlin
// ScheduleFixture.kt - ScheduleBuilder 내부
private var deletedAt: Instant? = null

fun deletedAt(deletedAt: Instant?): ScheduleBuilder = apply { this.deletedAt = deletedAt }
fun deleted(): ScheduleBuilder = apply { this.deletedAt = Instant.now() }

// build() 메서드 수정 - build 후 deletedAt 설정
fun build(): Schedule {
    val schedule = Schedule.createSchedule(
        memberId, departurePlace, arrivalPlace,
        preparationAlarm, departureAlarm, title,
        isRepeat, repeatDays, appointmentAt,
        isMedicationRequired, preparationNote
    )
    deletedAt?.let { schedule.deletedAt = it }
    return schedule
}
```

- [ ] **Step 2: 커밋**

```bash
git add src/test/kotlin/com/dh/ondot/schedule/fixture/ScheduleFixture.kt
git commit -m "test: ScheduleFixture에 deletedAt 빌더 메서드 추가"
```

---

### Task 3: 삭제 로직을 소프트 딜리트로 변경

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleService.kt`
- Modify: `src/test/kotlin/com/dh/ondot/schedule/domain/service/ScheduleServiceTest.kt`

- [ ] **Step 1: ScheduleService.deleteSchedule()을 소프트 딜리트로 변경**

```kotlin
// ScheduleService.kt
@Transactional
fun deleteSchedule(schedule: Schedule) {
    schedule.softDelete()
}
```

- [ ] **Step 2: ScheduleService.deleteAllByMemberId()를 소프트 딜리트로 변경**

```kotlin
// ScheduleService.kt
@Transactional
fun deleteAllByMemberId(memberId: Long) {
    val schedules = scheduleRepository.findAllByMemberId(memberId)
    schedules.forEach { it.softDelete() }
}
```

> `ScheduleRepository`에 `findAllByMemberId` 메서드 추가 필요

- [ ] **Step 3: ScheduleRepository에 findAllByMemberId 추가**

```kotlin
// ScheduleRepository.kt
fun findAllByMemberId(memberId: Long): List<Schedule>
```

> 기존 `deleteByMemberId`는 제거하지 않는다 (하위 호환성). 사용처만 변경.

- [ ] **Step 4: ScheduleServiceTest 수정 - 소프트 딜리트 검증**

```kotlin
// ScheduleServiceTest.kt - 기존 deleteSchedule 테스트 수정
@Test
@DisplayName("스케줄을 소프트 딜리트한다")
fun deleteSchedule_SoftDeletes() {
    // given
    val schedule = ScheduleFixture.defaultSchedule()
    assertThat(schedule.deletedAt).isNull()

    // when
    scheduleService.deleteSchedule(schedule)

    // then
    assertThat(schedule.deletedAt).isNotNull()
}
```

- [ ] **Step 5: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.domain.service.ScheduleServiceTest"`
Expected: PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleService.kt src/main/kotlin/com/dh/ondot/schedule/domain/repository/ScheduleRepository.kt src/test/kotlin/com/dh/ondot/schedule/domain/service/ScheduleServiceTest.kt
git commit -m "refactor: 스케줄 삭제를 하드 딜리트에서 소프트 딜리트로 변경"
```

---

### Task 4: 기존 조회 쿼리에 deletedAt IS NULL 조건 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/infra/ScheduleQueryRepository.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleQueryService.kt`
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/repository/ScheduleRepository.kt`

- [ ] **Step 1: ScheduleQueryRepository - QueryDSL 쿼리에 deletedAt IS NULL 추가**

```kotlin
// ScheduleQueryRepository.kt

// findScheduleById
fun findScheduleById(scheduleId: Long): Optional<Schedule> {
    val result = q.selectFrom(s)
        .join(s.departurePlace, dp).fetchJoin()
        .join(s.arrivalPlace, ap).fetchJoin()
        .where(s.id.eq(scheduleId), s.deletedAt.isNull)
        .fetchOne()
    return Optional.ofNullable(result)
}

// findScheduleByMemberIdAndId
fun findScheduleByMemberIdAndId(memberId: Long, scheduleId: Long): Optional<Schedule> {
    val result = q.selectFrom(s)
        .join(s.preparationAlarm, pa).fetchJoin()
        .join(s.departureAlarm, da).fetchJoin()
        .join(s.departurePlace, dp).fetchJoin()
        .join(s.arrivalPlace, ap).fetchJoin()
        .where(s.id.eq(scheduleId), s.memberId.eq(memberId), s.deletedAt.isNull)
        .fetchOne()
    return Optional.ofNullable(result)
}

// findActiveSchedulesByMember - .where 블록에 추가
.where(
    s.memberId.eq(memberId)
        .and(s.deletedAt.isNull)
        .and(s.isRepeat.isTrue.or(s.appointmentAt.goe(now)))
)
```

- [ ] **Step 2: ScheduleQueryService - findScheduleById에 deletedAt 검증 추가**

```kotlin
// ScheduleQueryService.kt
fun findScheduleById(id: Long): Schedule {
    val schedule = scheduleRepository.findById(id)
        .orElseThrow { NotFoundScheduleException(id) }
    if (schedule.isDeleted()) throw NotFoundScheduleException(id)
    return schedule
}
```

- [ ] **Step 3: ScheduleRepository - JPQL/메서드 쿼리에 조건 추가**

```kotlin
// ScheduleRepository.kt
@EntityGraph(attributePaths = ["preparationAlarm", "departureAlarm"])
fun findFirstByMemberIdAndDeletedAtIsNullOrderByUpdatedAtDesc(memberId: Long): Optional<Schedule>

@Query("SELECT s FROM Schedule s WHERE s.memberId IN :memberIds AND s.deletedAt IS NULL AND s.appointmentAt >= :start AND s.appointmentAt < :end")
fun findAllByMemberIdInAndAppointmentAtRange(
    @Param("memberIds") memberIds: List<Long>,
    @Param("start") start: Instant,
    @Param("end") end: Instant,
): List<Schedule>

fun findAllByMemberIdInAndIsRepeatTrueAndDeletedAtIsNull(memberIds: List<Long>): List<Schedule>
```

- [ ] **Step 4: ScheduleService에서 변경된 메서드명 반영**

```kotlin
// ScheduleService.kt - createScheduleWithAlarms 내부
val schedule = scheduleRepository.findFirstByMemberIdAndDeletedAtIsNullOrderByUpdatedAtDesc(member.id)
```

- [ ] **Step 5: 기존 호출처에서 변경된 메서드명 반영**

기존 `findAllByMemberIdInAndIsRepeatTrue` 호출처를 `findAllByMemberIdInAndIsRepeatTrueAndDeletedAtIsNull`로 변경. grep으로 호출처를 확인하여 모두 반영.

- [ ] **Step 6: 전체 테스트 실행**

Run: `./gradlew test`
Expected: PASS (기존 테스트가 깨지지 않는지 확인)

- [ ] **Step 7: 커밋**

```bash
git add -A
git commit -m "refactor: 기존 스케줄 조회 쿼리에 deletedAt IS NULL 조건 추가"
```

---

## Chunk 2: CalendarRecordExclusion 엔티티 + ErrorCode

### Task 5: ErrorCode에 캘린더 관련 에러코드 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/core/exception/InvalidCalendarDateRangeException.kt`

- [ ] **Step 1: ErrorCode에 캘린더 에러코드 추가**

```kotlin
// ErrorCode.kt - Everytime 섹션 뒤에 추가
// Calendar
INVALID_CALENDAR_DATE_RANGE(BAD_REQUEST, "캘린더 조회 날짜 범위가 올바르지 않습니다. startDate: %s, endDate: %s"),
CALENDAR_DATE_RANGE_TOO_LARGE(BAD_REQUEST, "캘린더 조회 범위는 최대 45일입니다. 요청 범위: %d일"),
```

- [ ] **Step 2: InvalidCalendarDateRangeException 생성**

```kotlin
package com.dh.ondot.schedule.core.exception

import com.dh.ondot.core.exception.BadRequestException
import com.dh.ondot.core.exception.ErrorCode

class InvalidCalendarDateRangeException(startDate: String, endDate: String) :
    BadRequestException(ErrorCode.INVALID_CALENDAR_DATE_RANGE.message.format(startDate, endDate)) {
    override val errorCode: String get() = ErrorCode.INVALID_CALENDAR_DATE_RANGE.name
}

class CalendarDateRangeTooLargeException(days: Long) :
    BadRequestException(ErrorCode.CALENDAR_DATE_RANGE_TOO_LARGE.message.format(days)) {
    override val errorCode: String get() = ErrorCode.CALENDAR_DATE_RANGE_TOO_LARGE.name
}
```

- [ ] **Step 3: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt src/main/kotlin/com/dh/ondot/schedule/core/exception/InvalidCalendarDateRangeException.kt
git commit -m "feat: 캘린더 관련 에러코드 및 예외 클래스 추가"
```

---

### Task 6: CalendarRecordExclusion 엔티티 + 리포지토리 + 서비스

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/CalendarRecordExclusion.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/repository/CalendarRecordExclusionRepository.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/service/CalendarRecordExclusionService.kt`
- Create: `src/test/kotlin/com/dh/ondot/schedule/domain/service/CalendarRecordExclusionServiceTest.kt`
- Create: `src/test/kotlin/com/dh/ondot/schedule/fixture/CalendarRecordExclusionFixture.kt`

- [ ] **Step 1: CalendarRecordExclusion 엔티티 생성**

```kotlin
package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "calendar_record_exclusions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_exclusion_member_schedule_date",
            columnNames = ["member_id", "schedule_id", "excluded_date"],
        )
    ],
)
class CalendarRecordExclusion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exclusion_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "schedule_id", nullable = false)
    val scheduleId: Long,

    @Column(name = "excluded_date", nullable = false)
    val excludedDate: LocalDate,
) : BaseTimeEntity() {

    companion object {
        fun create(memberId: Long, scheduleId: Long, excludedDate: LocalDate): CalendarRecordExclusion =
            CalendarRecordExclusion(
                memberId = memberId,
                scheduleId = scheduleId,
                excludedDate = excludedDate,
            )
    }
}
```

- [ ] **Step 2: CalendarRecordExclusionRepository 생성**

```kotlin
package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CalendarRecordExclusionRepository : JpaRepository<CalendarRecordExclusion, Long> {
    fun findAllByMemberIdAndExcludedDateBetween(
        memberId: Long, startDate: LocalDate, endDate: LocalDate,
    ): List<CalendarRecordExclusion>

    fun existsByMemberIdAndScheduleIdAndExcludedDate(
        memberId: Long, scheduleId: Long, excludedDate: LocalDate,
    ): Boolean
}
```

- [ ] **Step 3: CalendarRecordExclusionService 생성**

```kotlin
package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import com.dh.ondot.schedule.domain.repository.CalendarRecordExclusionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CalendarRecordExclusionService(
    private val repository: CalendarRecordExclusionRepository,
) {
    fun findExclusionsInRange(
        memberId: Long, startDate: LocalDate, endDate: LocalDate,
    ): List<CalendarRecordExclusion> =
        repository.findAllByMemberIdAndExcludedDateBetween(memberId, startDate, endDate)

    @Transactional
    fun excludeRecord(memberId: Long, scheduleId: Long, excludedDate: LocalDate) {
        if (repository.existsByMemberIdAndScheduleIdAndExcludedDate(memberId, scheduleId, excludedDate)) {
            return // 멱등성: 이미 존재하면 무시
        }
        repository.save(CalendarRecordExclusion.create(memberId, scheduleId, excludedDate))
    }
}
```

- [ ] **Step 4: CalendarRecordExclusionFixture 생성**

```kotlin
package com.dh.ondot.schedule.fixture

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import java.time.LocalDate

object CalendarRecordExclusionFixture {
    fun create(
        memberId: Long = 1L,
        scheduleId: Long = 1L,
        excludedDate: LocalDate = LocalDate.of(2026, 3, 14),
    ): CalendarRecordExclusion = CalendarRecordExclusion.create(memberId, scheduleId, excludedDate)
}
```

- [ ] **Step 5: CalendarRecordExclusionServiceTest 작성**

```kotlin
package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.CalendarRecordExclusion
import com.dh.ondot.schedule.domain.repository.CalendarRecordExclusionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarRecordExclusionService 테스트")
class CalendarRecordExclusionServiceTest {

    @Mock
    private lateinit var repository: CalendarRecordExclusionRepository

    @InjectMocks
    private lateinit var service: CalendarRecordExclusionService

    @Test
    @DisplayName("기록 제외 시 저장한다")
    fun excludeRecord_Saves() {
        // given
        given(repository.existsByMemberIdAndScheduleIdAndExcludedDate(1L, 1L, LocalDate.of(2026, 3, 14)))
            .willReturn(false)
        given(repository.save(any(CalendarRecordExclusion::class.java)))
            .willAnswer { it.arguments[0] }

        // when
        service.excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        // then
        verify(repository).save(any(CalendarRecordExclusion::class.java))
    }

    @Test
    @DisplayName("이미 제외된 기록은 무시한다 (멱등성)")
    fun excludeRecord_AlreadyExists_Skips() {
        // given
        given(repository.existsByMemberIdAndScheduleIdAndExcludedDate(1L, 1L, LocalDate.of(2026, 3, 14)))
            .willReturn(true)

        // when
        service.excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        // then
        verify(repository, never()).save(any())
    }
}
```

- [ ] **Step 6: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.domain.service.CalendarRecordExclusionServiceTest"`
Expected: PASS

- [ ] **Step 7: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/domain/CalendarRecordExclusion.kt src/main/kotlin/com/dh/ondot/schedule/domain/repository/CalendarRecordExclusionRepository.kt src/main/kotlin/com/dh/ondot/schedule/domain/service/CalendarRecordExclusionService.kt src/test/kotlin/com/dh/ondot/schedule/domain/service/CalendarRecordExclusionServiceTest.kt src/test/kotlin/com/dh/ondot/schedule/fixture/CalendarRecordExclusionFixture.kt
git commit -m "feat: CalendarRecordExclusion 엔티티, 리포지토리, 서비스 구현"
```

---

## Chunk 3: 캘린더 조회 핵심 로직

### Task 7: CalendarScheduleType enum + 응답 DTO

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/domain/enums/CalendarScheduleType.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/dto/CalendarDayItem.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/dto/CalendarScheduleItem.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/dto/CalendarDailyItem.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/CalendarRangeResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/CalendarDailyResponse.kt`

- [ ] **Step 1: CalendarScheduleType enum 생성**

```kotlin
package com.dh.ondot.schedule.domain.enums

enum class CalendarScheduleType {
    ALARM,  // 미래 스케줄
    RECORD, // 과거 기록
}
```

- [ ] **Step 2: CalendarScheduleItem (월별 조회용 DTO) 생성**

```kotlin
package com.dh.ondot.schedule.application.dto

import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import java.time.LocalDateTime

data class CalendarScheduleItem(
    val scheduleId: Long,
    val title: String,
    val type: CalendarScheduleType,
    val isRepeat: Boolean,
    val appointmentAt: LocalDateTime,
)
```

- [ ] **Step 3: CalendarDayItem (월별 조회 날짜별 그룹) 생성**

```kotlin
package com.dh.ondot.schedule.application.dto

import java.time.LocalDate

data class CalendarDayItem(
    val date: LocalDate,
    val schedules: List<CalendarScheduleItem>,
)
```

- [ ] **Step 4: CalendarDailyItem (일별 조회용 DTO) 생성**

```kotlin
package com.dh.ondot.schedule.application.dto

import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.presentation.response.AlarmDto
import java.time.LocalDateTime

data class CalendarDailyItem(
    val scheduleId: Long,
    val type: CalendarScheduleType,
    val title: String,
    val isRepeat: Boolean,
    val repeatDays: List<Int>,
    val appointmentAt: LocalDateTime,
    val preparationAlarm: AlarmDto?,
    val departureAlarm: AlarmDto?,
    val hasActiveAlarm: Boolean,
    val startLongitude: Double?,
    val startLatitude: Double?,
    val endLongitude: Double?,
    val endLatitude: Double?,
    val preparationNote: String?,
)
```

- [ ] **Step 5: CalendarRangeResponse 생성**

```kotlin
package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.CalendarDayItem
import java.time.LocalDate

data class CalendarRangeResponse(
    val days: List<CalendarDayResponse>,
) {
    data class CalendarDayResponse(
        val date: LocalDate,
        val schedules: List<CalendarScheduleResponse>,
    )

    data class CalendarScheduleResponse(
        val scheduleId: Long,
        val title: String,
        val type: String,
        val isRepeat: Boolean,
        val appointmentAt: String,
    )

    companion object {
        fun from(items: List<CalendarDayItem>): CalendarRangeResponse =
            CalendarRangeResponse(
                days = items.map { day ->
                    CalendarDayResponse(
                        date = day.date,
                        schedules = day.schedules.map { schedule ->
                            CalendarScheduleResponse(
                                scheduleId = schedule.scheduleId,
                                title = schedule.title,
                                type = schedule.type.name,
                                isRepeat = schedule.isRepeat,
                                appointmentAt = schedule.appointmentAt.toString(),
                            )
                        },
                    )
                },
            )
    }
}
```

- [ ] **Step 6: CalendarDailyResponse 생성**

```kotlin
package com.dh.ondot.schedule.presentation.response

import com.dh.ondot.schedule.application.dto.CalendarDailyItem

data class CalendarDailyResponse(
    val schedules: List<CalendarDailyScheduleResponse>,
) {
    data class CalendarDailyScheduleResponse(
        val scheduleId: Long,
        val type: String,
        val title: String,
        val isRepeat: Boolean,
        val repeatDays: List<Int>,
        val appointmentAt: String,
        val preparationAlarm: AlarmDto?,
        val departureAlarm: AlarmDto?,
        val hasActiveAlarm: Boolean,
        val startLongitude: Double?,
        val startLatitude: Double?,
        val endLongitude: Double?,
        val endLatitude: Double?,
        val preparationNote: String?,
    )

    companion object {
        fun from(items: List<CalendarDailyItem>): CalendarDailyResponse =
            CalendarDailyResponse(
                schedules = items.map { item ->
                    CalendarDailyScheduleResponse(
                        scheduleId = item.scheduleId,
                        type = item.type.name,
                        title = item.title,
                        isRepeat = item.isRepeat,
                        repeatDays = item.repeatDays,
                        appointmentAt = item.appointmentAt.toString(),
                        preparationAlarm = item.preparationAlarm,
                        departureAlarm = item.departureAlarm,
                        hasActiveAlarm = item.hasActiveAlarm,
                        startLongitude = item.startLongitude,
                        startLatitude = item.startLatitude,
                        endLongitude = item.endLongitude,
                        endLatitude = item.endLatitude,
                        preparationNote = item.preparationNote,
                    )
                },
            )
    }
}
```

- [ ] **Step 7: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/domain/enums/CalendarScheduleType.kt src/main/kotlin/com/dh/ondot/schedule/application/dto/Calendar*.kt src/main/kotlin/com/dh/ondot/schedule/presentation/response/Calendar*.kt
git commit -m "feat: 캘린더 조회 DTO 및 응답 클래스 구현"
```

---

### Task 8: CalendarQueryRepository (캘린더 전용 QueryDSL)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/infra/CalendarQueryRepository.kt`

- [ ] **Step 1: CalendarQueryRepository 생성**

이 리포지토리는 캘린더 조회에 필요한 스케줄을 가져온다. 반복일정은 애플리케이션 레이어에서 날짜별 확장 처리하므로, DB에서는 범위에 해당할 수 있는 후보 스케줄만 조회한다.

```kotlin
package com.dh.ondot.schedule.infra

import com.dh.ondot.schedule.domain.QAlarm
import com.dh.ondot.schedule.domain.QPlace
import com.dh.ondot.schedule.domain.QSchedule
import com.dh.ondot.schedule.domain.Schedule
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class CalendarQueryRepository(
    private val q: JPAQueryFactory,
) {
    companion object {
        private val s = QSchedule.schedule
        private val pa = QAlarm("calPa")
        private val da = QAlarm("calDa")
        private val dp = QPlace("calDp")
        private val ap = QPlace("calAp")
    }

    /**
     * 캘린더 범위 조회용: 범위 내 표시될 수 있는 모든 스케줄을 가져온다.
     * - 비반복 + deletedAt IS NULL: appointmentAt이 범위 내
     * - 비반복 + deletedAt IS NOT NULL: appointmentAt이 범위 내 AND appointmentAt < deletedAt (삭제 전 기록)
     * - 반복 + deletedAt IS NULL: createdAt <= 범위 끝 (날짜별 매칭은 앱 레이어에서)
     * - 반복 + deletedAt IS NOT NULL: createdAt <= 범위 끝 (삭제된 반복일정의 과거 기록)
     */
    fun findSchedulesForCalendarRange(
        memberId: Long, rangeStart: Instant, rangeEnd: Instant,
    ): List<Schedule> =
        q.selectFrom(s)
            .leftJoin(s.preparationAlarm, pa).fetchJoin()
            .leftJoin(s.departureAlarm, da).fetchJoin()
            .leftJoin(s.departurePlace, dp).fetchJoin()
            .leftJoin(s.arrivalPlace, ap).fetchJoin()
            .where(
                s.memberId.eq(memberId),
                // 비반복: appointmentAt이 범위 내
                // 반복: createdAt <= 범위 끝 (반복 확장은 앱에서 처리)
                s.isRepeat.isTrue.and(s.createdAt.loe(rangeEnd))
                    .or(
                        s.isRepeat.isFalse
                            .and(s.appointmentAt.goe(rangeStart))
                            .and(s.appointmentAt.lt(rangeEnd))
                    )
            )
            .orderBy(s.appointmentAt.asc())
            .fetch()
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/infra/CalendarQueryRepository.kt
git commit -m "feat: CalendarQueryRepository 캘린더 전용 QueryDSL 리포지토리 구현"
```

---

### Task 9: CalendarQueryFacade (핵심 조회 로직)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/CalendarQueryFacade.kt`
- Create: `src/test/kotlin/com/dh/ondot/schedule/application/CalendarQueryFacadeTest.kt`

- [ ] **Step 1: CalendarQueryFacade 생성**

```kotlin
package com.dh.ondot.schedule.application

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.application.dto.CalendarDailyItem
import com.dh.ondot.schedule.application.dto.CalendarDayItem
import com.dh.ondot.schedule.application.dto.CalendarScheduleItem
import com.dh.ondot.schedule.core.exception.CalendarDateRangeTooLargeException
import com.dh.ondot.schedule.core.exception.InvalidCalendarDateRangeException
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import com.dh.ondot.schedule.infra.CalendarQueryRepository
import com.dh.ondot.schedule.presentation.response.AlarmDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
@Transactional(readOnly = true)
class CalendarQueryFacade(
    private val memberService: MemberService,
    private val calendarQueryRepository: CalendarQueryRepository,
    private val exclusionService: CalendarRecordExclusionService,
) {
    companion object {
        private const val MAX_RANGE_DAYS = 45L
        private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")
    }

    fun getCalendarRange(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<CalendarDayItem> {
        validateDateRange(startDate, endDate)
        memberService.getMemberIfExists(memberId)

        val now = Instant.now()
        val rangeStart = startDate.atStartOfDay(SEOUL_ZONE).toInstant()
        val rangeEnd = endDate.plusDays(1).atStartOfDay(SEOUL_ZONE).toInstant()

        val schedules = calendarQueryRepository.findSchedulesForCalendarRange(memberId, rangeStart, rangeEnd)
        val exclusions = exclusionService.findExclusionsInRange(memberId, startDate, endDate)
        val excludedSet = exclusions.map { it.scheduleId to it.excludedDate }.toSet()

        val dayMap = mutableMapOf<LocalDate, MutableList<CalendarScheduleItem>>()

        for (schedule in schedules) {
            if (schedule.isRepeat) {
                expandRepeatSchedule(schedule, startDate, endDate, now, excludedSet, dayMap)
            } else {
                expandNonRepeatSchedule(schedule, startDate, endDate, now, excludedSet, dayMap)
            }
        }

        return dayMap.entries
            .sortedBy { it.key }
            .map { (date, items) -> CalendarDayItem(date, items.sortedBy { it.appointmentAt }) }
    }

    fun getCalendarDaily(memberId: Long, date: LocalDate): List<CalendarDailyItem> {
        memberService.getMemberIfExists(memberId)

        val now = Instant.now()
        val rangeStart = date.atStartOfDay(SEOUL_ZONE).toInstant()
        val rangeEnd = date.plusDays(1).atStartOfDay(SEOUL_ZONE).toInstant()

        val schedules = calendarQueryRepository.findSchedulesForCalendarRange(memberId, rangeStart, rangeEnd)
        val exclusions = exclusionService.findExclusionsInRange(memberId, date, date)
        val excludedSet = exclusions.map { it.scheduleId to it.excludedDate }.toSet()

        val items = mutableListOf<CalendarDailyItem>()

        for (schedule in schedules) {
            val appointmentDate = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalDate()
            val appointmentTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalTime() ?: continue

            if (schedule.isRepeat) {
                if (!schedule.isScheduledForDayOfWeek(date)) continue
                val createdDate = schedule.createdAt?.atZone(SEOUL_ZONE)?.toLocalDate() ?: continue
                if (createdDate.isAfter(date)) continue

                val appointmentInstant = date.atTime(appointmentTime).atZone(SEOUL_ZONE).toInstant()

                // 삭제된 스케줄: 삭제 시점 이후 기록 미표시
                if (schedule.isDeleted() && !schedule.deletedAt!!.isAfter(appointmentInstant)) continue

                val type = if (appointmentInstant.isBefore(now)) CalendarScheduleType.RECORD else CalendarScheduleType.ALARM

                // RECORD인 경우 제외 필터링
                if (type == CalendarScheduleType.RECORD && (schedule.id to date) in excludedSet) continue

                items.add(toDailyItem(schedule, type, date.atTime(appointmentTime)))
            } else {
                if (appointmentDate != date) continue

                // 삭제된 비반복 스케줄: 삭제가 약속시간 이전이면 미표시
                if (schedule.isDeleted() && !schedule.deletedAt!!.isAfter(schedule.appointmentAt)) continue

                val type = if (schedule.appointmentAt.isBefore(now)) CalendarScheduleType.RECORD else CalendarScheduleType.ALARM

                if (type == CalendarScheduleType.RECORD && (schedule.id to date) in excludedSet) continue

                items.add(toDailyItem(schedule, type, TimeUtils.toSeoulDateTime(schedule.appointmentAt)!!))
            }
        }

        return items.sortedBy { it.appointmentAt }
    }

    private fun expandRepeatSchedule(
        schedule: Schedule, startDate: LocalDate, endDate: LocalDate,
        now: Instant, excludedSet: Set<Pair<Long, LocalDate>>,
        dayMap: MutableMap<LocalDate, MutableList<CalendarScheduleItem>>,
    ) {
        val createdDate = schedule.createdAt?.atZone(SEOUL_ZONE)?.toLocalDate() ?: return
        val appointmentTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt)?.toLocalTime() ?: return

        var date = if (startDate.isAfter(createdDate)) startDate else createdDate
        while (!date.isAfter(endDate)) {
            if (schedule.isScheduledForDayOfWeek(date)) {
                val appointmentInstant = date.atTime(appointmentTime).atZone(SEOUL_ZONE).toInstant()

                // 삭제된 스케줄: 삭제 시점 이후 기록 미표시
                if (schedule.isDeleted() && !schedule.deletedAt!!.isAfter(appointmentInstant)) {
                    date = date.plusDays(1)
                    continue
                }

                val type = if (appointmentInstant.isBefore(now)) CalendarScheduleType.RECORD else CalendarScheduleType.ALARM

                // RECORD인 경우 제외 필터링
                if (type == CalendarScheduleType.RECORD && (schedule.id to date) in excludedSet) {
                    date = date.plusDays(1)
                    continue
                }

                dayMap.getOrPut(date) { mutableListOf() }.add(
                    CalendarScheduleItem(
                        scheduleId = schedule.id,
                        title = schedule.title,
                        type = type,
                        isRepeat = true,
                        appointmentAt = date.atTime(appointmentTime),
                    )
                )
            }
            date = date.plusDays(1)
        }
    }

    private fun expandNonRepeatSchedule(
        schedule: Schedule, startDate: LocalDate, endDate: LocalDate,
        now: Instant, excludedSet: Set<Pair<Long, LocalDate>>,
        dayMap: MutableMap<LocalDate, MutableList<CalendarScheduleItem>>,
    ) {
        val appointmentDateTime = TimeUtils.toSeoulDateTime(schedule.appointmentAt) ?: return
        val appointmentDate = appointmentDateTime.toLocalDate()

        if (appointmentDate.isBefore(startDate) || appointmentDate.isAfter(endDate)) return

        // 삭제된 비반복 스케줄: 삭제가 약속시간 이전이면 미표시
        if (schedule.isDeleted() && !schedule.deletedAt!!.isAfter(schedule.appointmentAt)) return

        val type = if (schedule.appointmentAt.isBefore(now)) CalendarScheduleType.RECORD else CalendarScheduleType.ALARM

        if (type == CalendarScheduleType.RECORD && (schedule.id to appointmentDate) in excludedSet) return

        dayMap.getOrPut(appointmentDate) { mutableListOf() }.add(
            CalendarScheduleItem(
                scheduleId = schedule.id,
                title = schedule.title,
                type = type,
                isRepeat = false,
                appointmentAt = appointmentDateTime,
            )
        )
    }

    private fun toDailyItem(schedule: Schedule, type: CalendarScheduleType, appointmentAt: java.time.LocalDateTime): CalendarDailyItem =
        CalendarDailyItem(
            scheduleId = schedule.id,
            type = type,
            title = schedule.title,
            isRepeat = schedule.isRepeat,
            repeatDays = schedule.repeatDays?.toList() ?: emptyList(),
            appointmentAt = appointmentAt,
            preparationAlarm = schedule.preparationAlarm?.let { AlarmDto.from(it) },
            departureAlarm = schedule.departureAlarm?.let { AlarmDto.from(it) },
            hasActiveAlarm = schedule.hasAnyActiveAlarm(),
            startLongitude = schedule.departurePlace?.longitude,
            startLatitude = schedule.departurePlace?.latitude,
            endLongitude = schedule.arrivalPlace?.longitude,
            endLatitude = schedule.arrivalPlace?.latitude,
            preparationNote = schedule.preparationNote,
        )

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        if (startDate.isAfter(endDate)) {
            throw InvalidCalendarDateRangeException(startDate.toString(), endDate.toString())
        }
        val days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)
        if (days > MAX_RANGE_DAYS) {
            throw CalendarDateRangeTooLargeException(days)
        }
    }
}
```

> **주의**: `AlarmDto.from(Alarm)` 팩토리 메서드가 기존에 없다면 추가해야 함. 기존 `AlarmDto` 파일을 확인하고, `from()` 메서드가 없으면 추가.

- [ ] **Step 2: CalendarQueryFacadeTest 작성**

```kotlin
package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.core.exception.CalendarDateRangeTooLargeException
import com.dh.ondot.schedule.core.exception.InvalidCalendarDateRangeException
import com.dh.ondot.schedule.domain.enums.CalendarScheduleType
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.fixture.ScheduleFixture
import com.dh.ondot.schedule.infra.CalendarQueryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarQueryFacade 테스트")
class CalendarQueryFacadeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var calendarQueryRepository: CalendarQueryRepository
    @Mock private lateinit var exclusionService: CalendarRecordExclusionService

    @InjectMocks private lateinit var facade: CalendarQueryFacade

    @Nested
    @DisplayName("날짜 범위 검증")
    inner class DateRangeValidation {
        @Test
        @DisplayName("startDate > endDate이면 예외 발생")
        fun invalidRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(1L, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 1))
            }.isInstanceOf(InvalidCalendarDateRangeException::class.java)
        }

        @Test
        @DisplayName("45일 초과 범위이면 예외 발생")
        fun tooLargeRange_ThrowsException() {
            assertThatThrownBy {
                facade.getCalendarRange(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1))
            }.isInstanceOf(CalendarDateRangeTooLargeException::class.java)
        }
    }

    @Nested
    @DisplayName("범위 조회")
    inner class RangeQuery {
        @Test
        @DisplayName("스케줄이 없으면 빈 리스트를 반환한다")
        fun noSchedules_ReturnsEmpty() {
            // given
            val startDate = LocalDate.of(2026, 3, 1)
            val endDate = LocalDate.of(2026, 3, 31)
            given(calendarQueryRepository.findSchedulesForCalendarRange(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(Instant::class.java),
                org.mockito.ArgumentMatchers.any(Instant::class.java),
            )).willReturn(emptyList())
            given(exclusionService.findExclusionsInRange(1L, startDate, endDate)).willReturn(emptyList())

            // when
            val result = facade.getCalendarRange(1L, startDate, endDate)

            // then
            assertThat(result).isEmpty()
        }
    }
}
```

- [ ] **Step 3: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.application.CalendarQueryFacadeTest"`
Expected: PASS

- [ ] **Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/application/CalendarQueryFacade.kt src/test/kotlin/com/dh/ondot/schedule/application/CalendarQueryFacadeTest.kt
git commit -m "feat: CalendarQueryFacade 캘린더 범위/일별 조회 핵심 로직 구현"
```

---

## Chunk 4: API 레이어 + Swagger

### Task 10: CalendarCommandFacade (기록 삭제)

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/application/CalendarCommandFacade.kt`
- Create: `src/test/kotlin/com/dh/ondot/schedule/application/CalendarCommandFacadeTest.kt`

- [ ] **Step 1: CalendarCommandFacade 생성**

```kotlin
package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CalendarCommandFacade(
    private val memberService: MemberService,
    private val scheduleQueryService: ScheduleQueryService,
    private val exclusionService: CalendarRecordExclusionService,
) {
    @Transactional
    fun deleteCalendarRecord(memberId: Long, scheduleId: Long, date: LocalDate) {
        memberService.getMemberIfExists(memberId)
        // 소유권 검증: 해당 스케줄이 사용자의 것인지 확인
        scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId)
        exclusionService.excludeRecord(memberId, scheduleId, date)
    }
}
```

> **주의**: `findScheduleByMemberIdAndId`는 `deletedAt IS NULL` 조건이 있으므로, 삭제된 스케줄의 기록은 삭제할 수 없다. 삭제된 스케줄의 과거 기록도 삭제할 수 있어야 하는 경우, 별도 메서드 필요. 현재 정책으로는 삭제된 스케줄은 캘린더에 표시되지 않으므로 (deletedAt 이후 기록 미표시), 기록 삭제 요청 자체가 불필요. 소프트 딜리트된 스케줄의 과거 기록(deletedAt 이전)에 대해서는 scheduleId로 직접 조회 필요. 이 부분은 ScheduleQueryService에 `findScheduleByMemberIdAndIdIncludingDeleted` 메서드를 추가.

```kotlin
// ScheduleQueryService.kt에 추가
fun findScheduleByMemberIdAndIdIncludingDeleted(memberId: Long, scheduleId: Long): Schedule =
    scheduleQueryRepository.findScheduleByMemberIdAndIdIncludingDeleted(memberId, scheduleId)
        .orElseThrow { NotFoundScheduleException(scheduleId) }
```

```kotlin
// ScheduleQueryRepository.kt에 추가
fun findScheduleByMemberIdAndIdIncludingDeleted(memberId: Long, scheduleId: Long): Optional<Schedule> {
    val result = q.selectFrom(s)
        .where(s.id.eq(scheduleId), s.memberId.eq(memberId))
        .fetchOne()
    return Optional.ofNullable(result)
}
```

CalendarCommandFacade에서는 이 메서드를 사용:

```kotlin
// CalendarCommandFacade.kt 수정
scheduleQueryService.findScheduleByMemberIdAndIdIncludingDeleted(memberId, scheduleId)
```

- [ ] **Step 2: CalendarCommandFacadeTest 작성**

```kotlin
package com.dh.ondot.schedule.application

import com.dh.ondot.member.domain.service.MemberService
import com.dh.ondot.schedule.domain.service.CalendarRecordExclusionService
import com.dh.ondot.schedule.domain.service.ScheduleQueryService
import com.dh.ondot.schedule.fixture.ScheduleFixture
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("CalendarCommandFacade 테스트")
class CalendarCommandFacadeTest {

    @Mock private lateinit var memberService: MemberService
    @Mock private lateinit var scheduleQueryService: ScheduleQueryService
    @Mock private lateinit var exclusionService: CalendarRecordExclusionService

    @InjectMocks private lateinit var facade: CalendarCommandFacade

    @Test
    @DisplayName("캘린더 기록 삭제 시 소유권 검증 후 제외 기록을 생성한다")
    fun deleteCalendarRecord_VerifiesOwnershipAndExcludes() {
        // given
        val schedule = ScheduleFixture.defaultSchedule()
        given(scheduleQueryService.findScheduleByMemberIdAndIdIncludingDeleted(1L, 1L))
            .willReturn(schedule)

        // when
        facade.deleteCalendarRecord(1L, 1L, LocalDate.of(2026, 3, 14))

        // then
        verify(memberService).getMemberIfExists(1L)
        verify(exclusionService).excludeRecord(1L, 1L, LocalDate.of(2026, 3, 14))
    }
}
```

- [ ] **Step 3: 테스트 실행**

Run: `./gradlew test --tests "com.dh.ondot.schedule.application.CalendarCommandFacadeTest"`
Expected: PASS

- [ ] **Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/application/CalendarCommandFacade.kt src/test/kotlin/com/dh/ondot/schedule/application/CalendarCommandFacadeTest.kt src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleQueryService.kt src/main/kotlin/com/dh/ondot/schedule/infra/ScheduleQueryRepository.kt
git commit -m "feat: CalendarCommandFacade 기록 삭제 로직 구현"
```

---

### Task 11: CalendarController + CalendarSwagger

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/CalendarController.kt`
- Create: `src/main/kotlin/com/dh/ondot/schedule/presentation/swagger/CalendarSwagger.kt`

- [ ] **Step 1: CalendarSwagger 인터페이스 생성**

```kotlin
package com.dh.ondot.schedule.presentation.swagger

import com.dh.ondot.schedule.presentation.response.CalendarDailyResponse
import com.dh.ondot.schedule.presentation.response.CalendarRangeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Calendar API", description = "캘린더 조회 및 기록 관리 API")
interface CalendarSwagger {

    @Operation(summary = "캘린더 범위 조회", description = "시작일~종료일 범위의 스케줄을 날짜별로 그룹핑하여 조회한다. 최대 45일.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "400", description = "날짜 범위 오류")
    fun getCalendarRange(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "시작일 (yyyy-MM-dd)", example = "2026-03-01") startDate: String,
        @Parameter(description = "종료일 (yyyy-MM-dd)", example = "2026-03-31") endDate: String,
    ): CalendarRangeResponse

    @Operation(summary = "캘린더 일별 조회", description = "특정 날짜의 스케줄을 상세 조회한다. 시간 빠른 순 정렬.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getCalendarDaily(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "조회일 (yyyy-MM-dd)", example = "2026-03-14") date: String,
    ): CalendarDailyResponse

    @Operation(summary = "캘린더 기록 삭제", description = "특정 날짜의 과거 기록을 캘린더에서 제거한다. 멱등성 보장.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "스케줄 미존재")
    fun deleteCalendarRecord(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "스케줄 ID") scheduleId: Long,
        @Parameter(description = "제외할 날짜 (yyyy-MM-dd)", example = "2026-03-14") date: String,
    )
}
```

- [ ] **Step 2: CalendarController 생성**

```kotlin
package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.application.CalendarCommandFacade
import com.dh.ondot.schedule.application.CalendarQueryFacade
import com.dh.ondot.schedule.presentation.response.CalendarDailyResponse
import com.dh.ondot.schedule.presentation.response.CalendarRangeResponse
import com.dh.ondot.schedule.presentation.swagger.CalendarSwagger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/calendar")
class CalendarController(
    private val calendarQueryFacade: CalendarQueryFacade,
    private val calendarCommandFacade: CalendarCommandFacade,
) : CalendarSwagger {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    override fun getCalendarRange(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
    ): CalendarRangeResponse {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        val items = calendarQueryFacade.getCalendarRange(memberId, start, end)
        return CalendarRangeResponse.from(items)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{date}")
    override fun getCalendarDaily(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable date: String,
    ): CalendarDailyResponse {
        val localDate = LocalDate.parse(date)
        val items = calendarQueryFacade.getCalendarDaily(memberId, localDate)
        return CalendarDailyResponse.from(items)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/records")
    override fun deleteCalendarRecord(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam scheduleId: Long,
        @RequestParam date: String,
    ) {
        val localDate = LocalDate.parse(date)
        calendarCommandFacade.deleteCalendarRecord(memberId, scheduleId, localDate)
    }
}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/presentation/CalendarController.kt src/main/kotlin/com/dh/ondot/schedule/presentation/swagger/CalendarSwagger.kt
git commit -m "feat: CalendarController REST API 엔드포인트 및 Swagger 문서 구현"
```

---

## Chunk 5: 통합 검증

### Task 12: AlarmDto.from() 팩토리 메서드 확인 및 추가

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/presentation/response/AlarmDto.kt` (필요 시)

- [ ] **Step 1: AlarmDto에 from(Alarm) 팩토리 메서드가 있는지 확인**

기존 `AlarmDto`를 읽고 `from()` 메서드가 없으면 추가:

```kotlin
companion object {
    fun from(alarm: Alarm): AlarmDto = AlarmDto(
        alarmId = alarm.id,
        alarmMode = alarm.mode.name,
        isEnabled = alarm.isEnabled,
        triggeredAt = TimeUtils.toSeoulDateTime(alarm.triggeredAt),
        isSnoozeEnabled = alarm.snooze.isSnoozeEnabled,
        snoozeInterval = alarm.snooze.snoozeInterval.minutes,
        snoozeCount = alarm.snooze.snoozeCount.count,
        soundCategory = alarm.sound.soundCategory.name,
        ringTone = alarm.sound.ringTone.name,
        volume = alarm.sound.volume,
    )
}
```

- [ ] **Step 2: 커밋 (변경 있는 경우만)**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/presentation/response/AlarmDto.kt
git commit -m "feat: AlarmDto에 from(Alarm) 팩토리 메서드 추가"
```

---

### Task 13: 전체 테스트 + 빌드 검증

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 2: 애플리케이션 빌드 검증**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 누락 확인 - 기존 코드에서 변경된 메서드명을 사용하는 곳 검색**

```bash
# 기존 메서드명이 남아있는지 확인
grep -r "findFirstByMemberIdOrderByUpdatedAtDesc" src/main/ --include="*.kt"
grep -r "findAllByMemberIdInAndIsRepeatTrue\b" src/main/ --include="*.kt"
grep -r "deleteByMemberId" src/main/ --include="*.kt"
```

모든 호출처가 새 메서드명으로 변경되었는지 확인.

- [ ] **Step 4: 최종 커밋 (변경사항 있는 경우)**

```bash
git add -A
git commit -m "fix: 기존 코드에서 변경된 메서드명 참조 수정"
```
