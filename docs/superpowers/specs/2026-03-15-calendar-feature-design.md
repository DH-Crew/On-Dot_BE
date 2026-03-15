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

## API 설계

### 1. 캘린더 범위 조회

```
GET /calendar?startDate={startDate}&endDate={endDate}
```

클라이언트가 캘린더 뷰에 보이는 시작/끝 날짜를 전달한다. 스케줄이 없는 날짜는 응답에서 생략한다.

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
      "scheduleTitle": "학교 수업",
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

### 3. 캘린더 기록 삭제

```
DELETE /calendar/records?scheduleId={scheduleId}&date={date}
```

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

### 미래 스케줄 (type = ALARM)

- 조건: `deletedAt IS NULL`
- 비반복: `appointmentAt >= now` AND `appointmentAt`이 조회 범위 내
- 반복: `repeatDays`가 조회 범위 내 날짜와 매칭 AND `createdAt <= 해당 날짜`

### 과거 기록 (type = RECORD)

- 비반복: `appointmentAt < now` AND `appointmentAt`이 조회 범위 내 AND (`deletedAt IS NULL` OR `deletedAt > appointmentAt`)
- 반복: 조회 범위 내 각 과거 날짜에 대해 `createdAt <= 해당 날짜` AND `repeatDays` 매칭 AND (`deletedAt IS NULL` OR `deletedAt > 해당 날짜의 약속시간`)

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
- `ScheduleRepository.findFirstByMemberIdOrderByUpdatedAtDesc()`
- `ScheduleRepository.findAllByMemberIdInAndAppointmentAtRange()`
- `ScheduleRepository.findAllByMemberIdInAndIsRepeatTrue()`

### 삭제 로직 변경

- `ScheduleService.deleteSchedule()`: `repository.delete()` → `schedule.deletedAt = Instant.now()`
- `ScheduleService.deleteAllByMemberId()`: 회원 탈퇴 시 하드 딜리트 유지

## 성능 고려사항

- 쿼리 타임 계산: 유저당 반복 스케줄 수가 현실적으로 수십 개 수준이라 메모리/CPU 부담 없음
- CalendarRecordExclusion: 조회 범위 내 데이터만 가져오므로 소량
- 인덱스: `Schedule(member_id, is_repeat, created_at)`, `CalendarRecordExclusion(member_id, excluded_date)`

## 향후 확장

외부 캘린더(구글, 애플) 연동 시:
- `CalendarRecord` 테이블 추가 (`sourceType`: GOOGLE, APPLE 등)
- 초기 동기화 + Webhook(Push Notification)으로 변경분 incremental sync
- 캘린더 조회 시 Schedule(온닷) + CalendarRecord(외부) 합쳐서 응답
