# DH-21: 캘린더 기능 설계

## 개요

알람을 캘린더 형식으로 보여주는 뷰. 과거 기록과 미래 알람을 통합 조회한다.

- **과거 기록(RECORD)**: 약속 시간이 현재 시각보다 이전인 스케줄. 알람 on/off 여부와 무관하게 약속 시간 경과 기준으로 판단한다.
- **미래 알람(ALARM)**: 약속 시간이 현재 시각 이후인 스케줄.

## 범위

### 포함
- Schedule 소프트 딜리트 전환 (`deletedAt` 추가)
- 캘린더 범위 조회 API
- 캘린더 일별 조회 API
- 캘린더 기록 삭제 API
- 기존 삭제 API를 하드 딜리트 → 소프트 딜리트로 변경
- 기존 조회 쿼리에 `deletedAt IS NULL` 조건 추가

### 미포함 (향후)
- CalendarRecord 테이블 (외부 캘린더 연동 시 생성)
- 구글/애플 캘린더 연동

## 데이터 모델

### Schedule 엔티티 변경

`deletedAt: Instant?` 필드를 추가한다. nullable이므로 `columnDefinition` 불필요.

```kotlin
@Column(name = "deleted_at")
var deletedAt: Instant? = null
```

삭제 시 `schedule.deletedAt = Instant.now()`로 소프트 딜리트한다.
회원 탈퇴 시에는 개인정보 삭제 의무로 기존 하드 딜리트를 유지한다.

> **`@SQLRestriction` 미사용 이유**: Hibernate 6의 `@SQLRestriction("deleted_at IS NULL")`을 사용하면 모든 쿼리에 자동 적용되어 편리하지만, 회원 탈퇴 시 하드 딜리트를 위해 이미 소프트 딜리트된 스케줄을 조회해야 하는 케이스가 존재한다. 이를 우회하려면 native query나 직접 JDBC가 필요해지므로, 수동으로 `deletedAt IS NULL` 조건을 추가하는 방식을 택한다.

### CalendarRecordExclusion 테이블 (신규)

사용자가 캘린더에서 개별 과거 기록을 삭제할 때 사용한다.

```
CalendarRecordExclusion
- id: Long (PK)
- memberId: Long
- scheduleId: Long
- excludedDate: LocalDate
- createdAt: Instant
- UNIQUE(memberId, scheduleId, excludedDate)
```

회원 탈퇴 시 해당 memberId의 CalendarRecordExclusion도 함께 하드 딜리트한다 (개인정보 삭제).

## API 설계

### 1. 캘린더 범위 조회

```
GET /calendar?startDate={startDate}&endDate={endDate}
```

클라이언트가 캘린더 뷰에 보이는 시작/끝 날짜를 전달한다. 스케줄이 없는 날짜는 응답에서 생략한다.

**검증 규칙:**
- `startDate <= endDate`이어야 한다. 위반 시 400 Bad Request.
- 최대 조회 범위: 45일 (캘린더 뷰 6주 = 42일 + 여유분). 위반 시 400 Bad Request.

**Response:**
```json
{
  "days": [
    {
      "date": "2026-03-14",
      "schedules": [
        {
          "scheduleId": 1,
          "title": "학교 수업",
          "type": "ALARM",
          "isRepeat": true,
          "appointmentAt": "2026-03-14T09:00:00"
        }
      ]
    }
  ]
}
```

- `type`: `ALARM`(미래) | `RECORD`(과거). 기준은 `Instant.now()`.
- 모든 datetime 필드는 KST(Asia/Seoul) 기준 `LocalDateTime`으로 응답한다 (기존 API 컨벤션과 동일).

### 2. 캘린더 일별 조회

```
GET /calendar/{date}
```

해당 날짜의 스케줄을 시간 빠른 순으로 정렬. 페이지네이션 없음.
응답 구조는 기존 `HomeScheduleListItem`과 동일하되, `type` 필드가 추가된다.

**Response:**
```json
{
  "schedules": [
    {
      "scheduleId": 1,
      "type": "ALARM",
      "title": "학교 수업",
      "isRepeat": true,
      "repeatDays": [2, 3, 4, 5, 6],
      "appointmentAt": "2026-03-14T09:00:00",
      "preparationAlarm": {
        "alarmId": 1,
        "alarmMode": "SOUND",
        "isEnabled": true,
        "triggeredAt": "2026-03-14T08:30:00",
        "isSnoozeEnabled": false,
        "snoozeInterval": 5,
        "snoozeCount": 1,
        "soundCategory": "BRIGHT_ENERGY",
        "ringTone": "DANCING_IN_THE_STARDUST",
        "volume": 0.8
      },
      "departureAlarm": { "..." : "..." },
      "hasActiveAlarm": true,
      "startLongitude": 127.0,
      "startLatitude": 37.5,
      "endLongitude": 127.1,
      "endLatitude": 37.6,
      "preparationNote": "우산 챙기기"
    }
  ]
}
```

> 필드명은 범위 조회와 통일하여 `title`을 사용한다. `nextAlarmAt`은 캘린더 뷰에서 불필요하므로 생략한다.

### 3. 캘린더 기록 삭제

```
DELETE /calendar/records?scheduleId={scheduleId}&date={date}
```

- 요청한 `scheduleId`가 인증된 사용자의 소유인지 검증한다
- `CalendarRecordExclusion` 생성
- 이미 존재하면 무시 (멱등)
- 204 No Content 응답

### 4. 기존 스케줄 삭제 (변경)

```
DELETE /schedules/{id}
```

하드 딜리트 → 소프트 딜리트(`deletedAt = Instant.now()`)로 변경. 응답 변경 없음.

## 캘린더 조회 핵심 로직

별도 CalendarRecord 테이블 없이, Schedule 테이블에서 쿼리 타임에 계산하여 도출한다.

### 타임존 처리

`createdAt`, `deletedAt`, `appointmentAt`은 모두 `Instant`(UTC)로 저장된다. 조회 범위의 `LocalDate`와 비교할 때는 **Asia/Seoul 기준으로 변환**하여 비교한다.

```kotlin
// 예: 특정 날짜의 약속시간 Instant 계산
val appointmentTimeOnDate = date.atTime(schedule.appointmentTime)
    .atZone(ZoneId.of("Asia/Seoul"))
    .toInstant()
```

### 미래 스케줄 (type = ALARM)

- 조건: `deletedAt IS NULL`
- 비반복: `appointmentAt >= now` AND `appointmentAt`이 조회 범위 내
- 반복: `repeatDays`가 조회 범위 내 날짜와 매칭 AND `createdAt`(서울 시간 변환) `<= 해당 날짜`

### 과거 기록 (type = RECORD)

- 비반복: `appointmentAt < now` AND `appointmentAt`이 조회 범위 내 AND (`deletedAt IS NULL` OR `deletedAt > appointmentAt`)
- 반복: 조회 범위 내 각 과거 날짜에 대해 `createdAt`(서울 시간 변환) `<= 해당 날짜` AND `repeatDays` 매칭 AND (`deletedAt IS NULL` OR `deletedAt > 해당 날짜의 약속시간`)

### 제외 필터링

조회 범위 내 `CalendarRecordExclusion`을 조회하여 과거 기록에서 제외한다.

```sql
SELECT * FROM calendar_record_exclusion
WHERE member_id = ? AND excluded_date BETWEEN startDate AND endDate
```

### 날짜별 그룹핑

미래 + 과거 결과를 합쳐서 날짜별로 그룹핑. 스케줄 없는 날짜는 생략.

## 기존 코드 변경사항

### 쿼리 조건 추가 (deletedAt IS NULL)

다음 쿼리에 소프트 딜리트 조건을 추가한다:

- `ScheduleQueryRepository.findActiveSchedulesByMember()`
- `ScheduleQueryRepository.findScheduleByIdEager()`
- `ScheduleQueryService.findScheduleById()` — `repository.findById()` 이후 `deletedAt != null`이면 예외 처리
- `ScheduleQueryService.findScheduleByMemberIdAndId()` — 동일하게 `deletedAt` 검증
- `ScheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc()` — 삭제된 스케줄에서 알람 설정 복사 방지
- `ScheduleRepository.findAllByMemberIdInAndAppointmentAtRange()`
- `ScheduleRepository.findAllByMemberIdInAndIsRepeatTrue()`

### 삭제 로직 변경

- `ScheduleService.deleteSchedule()`: `repository.delete()` → `schedule.deletedAt = Instant.now()`
- `ScheduleService.deleteAllByMemberId()`: 회원 탈퇴 시 하드 딜리트 유지 + CalendarRecordExclusion도 함께 삭제

## 성능 고려사항

- 쿼리 타임 계산: 유저당 반복 스케줄 수가 현실적으로 수십 개 수준이라 메모리/CPU 부담 없음
- API 최대 조회 범위 45일 제한으로 반복 스케줄 레코드 폭증 방지
- CalendarRecordExclusion: 조회 범위 내 데이터만 가져오므로 소량
- 인덱스: `Schedule(member_id, deleted_at, is_repeat)`, `CalendarRecordExclusion(member_id, excluded_date)`

## 향후 확장

외부 캘린더(구글, 애플) 연동 시:
- `CalendarRecord` 테이블 추가 (`sourceType`: GOOGLE, APPLE 등)
- 초기 동기화 + Webhook(Push Notification)으로 변경분 incremental sync
- 캘린더 조회 시 Schedule(온닷) + CalendarRecord(외부) 합쳐서 응답
