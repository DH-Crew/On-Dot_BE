# DH-28: 알람 isEnabled 기본값 수정 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 새 스케줄 생성 시 이전 스케줄의 알람 비활성화 상태가 복사되는 버그를 수정하여, 항상 `isEnabled=true`로 응답하도록 한다.

**Architecture:** `ScheduleService.createFromLatestUserSetting()`에서 알람 복사 후 `isEnabled`를 `true`로 리셋. TDD로 실패 테스트 먼저 작성 후 수정.

**Tech Stack:** Kotlin, Spring Boot, JUnit 5, Mockito, Gradle

---

### Task 1: 실패 테스트 작성

**Files:**
- Modify: `src/test/kotlin/com/dh/ondot/schedule/domain/service/ScheduleServiceTest.kt:53-73`

**Step 1: 실패 테스트 작성**

기존 `setupSchedule_WithExistingSchedule_CopiesFromLatestSetting` 테스트 아래에 새 테스트를 추가한다. 이전 스케줄의 알람이 꺼져있을 때, 새 스케줄의 `isEnabled`가 `true`인지 검증한다.

```kotlin
@Test
@DisplayName("기존 스케줄의 알람이 꺼져있어도 새 스케줄의 알람은 활성화 상태로 생성한다")
fun setupSchedule_WithDisabledAlarms_CreatesWithEnabledAlarms() {
    // given
    val member = MemberFixture.defaultMember()
    val appointmentAt = LocalDateTime.of(2025, 12, 16, 14, 0)
    val estimatedTimeMin = 30
    val latestSchedule = ScheduleFixture.builder().disabledAlarms().build()

    given(scheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc(member.id))
        .willReturn(Optional.of(latestSchedule))

    // when
    val result = scheduleService.setupSchedule(member, appointmentAt, estimatedTimeMin)

    // then
    assertThat(result.preparationAlarm!!.isEnabled).isTrue()
    assertThat(result.departureAlarm!!.isEnabled).isTrue()
}
```

이 테스트는 `ScheduleFixture.builder().disabledAlarms().build()`로 `isEnabled=false`인 스케줄을 생성하고, 이를 기반으로 새 스케줄을 만들었을 때 `isEnabled=true`인지 검증한다.

**Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.dh.ondot.schedule.domain.service.ScheduleServiceTest.setupSchedule_WithDisabledAlarms_CreatesWithEnabledAlarms" --no-daemon`
Expected: FAIL — `assertThat(result.preparationAlarm!!.isEnabled).isTrue()` 에서 `expected: true but was: false`

**Step 3: 커밋**

```bash
git add src/test/kotlin/com/dh/ondot/schedule/domain/service/ScheduleServiceTest.kt
git commit -m "test: 비활성화된 알람 복사 시 isEnabled 검증 테스트 추가"
```

---

### Task 2: 버그 수정

**Files:**
- Modify: `src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleService.kt:33-44`

**Step 1: `createFromLatestUserSetting()`에서 isEnabled 리셋 코드 추가**

`ScheduleService.kt`의 `createFromLatestUserSetting()` 메서드에서, `copySchedule()` 호출 직후에 양쪽 알람의 `isEnabled`를 `true`로 리셋한다:

```kotlin
private fun createFromLatestUserSetting(
    latestSchedule: Schedule, member: Member,
    appointment: LocalDateTime, estimatedTimeMin: Int,
): Schedule {
    val copy = copySchedule(latestSchedule)
    copy.preparationAlarm!!.changeEnabled(true)
    copy.departureAlarm!!.changeEnabled(true)
    val depAlarmAt = appointment.minusMinutes(estimatedTimeMin.toLong())
    val prepAlarmAt = depAlarmAt.minusMinutes(member.preparationTime!!.toLong())
    copy.departureAlarm!!.updateTriggeredAt(depAlarmAt)
    copy.preparationAlarm!!.updateTriggeredAt(prepAlarmAt)

    return copy
}
```

**Step 2: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.dh.ondot.schedule.domain.service.ScheduleServiceTest" --no-daemon`
Expected: ALL PASS — 새로 추가한 테스트 포함 전체 통과

**Step 3: 전체 테스트 실행**

Run: `./gradlew test --no-daemon`
Expected: ALL PASS — 다른 테스트에 영향 없음 확인

**Step 4: 커밋**

```bash
git add src/main/kotlin/com/dh/ondot/schedule/domain/service/ScheduleService.kt
git commit -m "fix: 새 스케줄 생성 시 알람 isEnabled를 항상 true로 초기화"
```
