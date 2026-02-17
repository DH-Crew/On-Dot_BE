# DH-28: 일정생성 시 알람 isEnabled 기본값 수정

## 문제

`setAlarm` 엔드포인트가 새 스케줄의 알람 기본 설정을 UI에 반환할 때, 이전 스케줄에서 알람을 껐었다면 `isEnabled=false`가 그대로 복사되어 UI에서 알람이 꺼진 것으로 표시됨.

### 원인

`ScheduleService.createFromLatestUserSetting()`에서 `Alarm.copy()`로 이전 스케줄의 알람을 복사할 때 `isEnabled` 상태가 그대로 복사됨. `triggeredAt`만 업데이트하고 `isEnabled`는 리셋하지 않음.

### 영향 범위

- `ScheduleService.createFromLatestUserSetting()` — 버그 소재지
- `Alarm.copy()` — isEnabled를 포함해 전체 필드를 복사

## 수정 접근

**접근 방식 A 채택**: `createFromLatestUserSetting()`에서 copy 후 `isEnabled=true`로 리셋

### 변경 파일

1. `ScheduleService.kt` — `createFromLatestUserSetting()`에서 copy 후 `changeEnabled(true)` 호출
2. `ScheduleServiceTest.kt` — 이전 스케줄 알람이 꺼져있을 때 새 스케줄의 `isEnabled=true` 검증 테스트 추가

### 변경하지 않는 것

- `Alarm.copy()` — 다른 곳에서 사용될 수 있으므로 변경하지 않음
- `copySchedule()` — 기존 복사 로직 유지
- mode, snooze, sound 등 사용자 설정 상속은 기존대로 유지
