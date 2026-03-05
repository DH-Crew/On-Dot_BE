# DH-85: 에브리타임 연동 API 수정 설계

## 요약

에브리타임 시간표 연동 흐름을 **서버 자동 선택** → **유저 선택 기반**으로 변경한다.

## API 스펙

### POST `/api/v1/schedules/everytime/validate`

**Request** (변경 없음):
```json
{ "everytimeUrl": "https://everytime.kr/@{identifier}" }
```

**Response** (변경):
```json
{
  "timetable": {
    "MONDAY": [
      { "courseName": "드론과 로보틱스", "startTime": "10:00", "endTime": "12:00" },
      { "courseName": "시스템아키텍처", "startTime": "12:00", "endTime": "14:00" }
    ],
    "TUESDAY": [...]
  }
}
```

- 요일 키: MONDAY~SUNDAY, 월요일부터 정렬
- 요일 내 수업: startTime 오름차순
- 수업 없는 요일: 키 생략

### POST `/api/v1/schedules/everytime`

**Request** (변경):
```json
{
  "departurePlace": { "title": "...", "roadAddress": "...", "longitude": 0.0, "latitude": 0.0 },
  "arrivalPlace": { "title": "...", "roadAddress": "...", "longitude": 0.0, "latitude": 0.0 },
  "transportType": "PUBLIC_TRANSPORT",
  "selectedLectures": [
    { "day": "MONDAY", "startTime": "10:00" },
    { "day": "THURSDAY", "startTime": "10:00" }
  ]
}
```

- `everytimeUrl` 제거, `identifier` 불필요
- `selectedLectures`: 요일별 최대 1개, 없는 요일 생략

**Response** (변경 없음)

## 내부 로직

### validate
URL → identifier 추출 → 에브리타임 API 호출 → EverytimeLecture 목록을 요일별 그룹핑 + 시간순 정렬 → timetable 반환

### create
selectedLectures 수신 → 같은 startTime끼리 그룹핑 → 기존 스케줄 생성 로직 (경로 계산, 알람 생성)

- 에브리타임 API 호출 제거
- 자동 선택 로직 제거
- 유저 선택 기반 그룹핑

## 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| EverytimeValidateResponse | identifier → timetable |
| 신규: TimetableEntry | courseName, startTime, endTime |
| EverytimeScheduleCreateRequest | everytimeUrl 제거, selectedLectures 추가 |
| 신규: SelectedLecture | day, startTime |
| ScheduleCommandFacade | validate/create 로직 변경 |
| CreateEverytimeScheduleCommand | everytimeUrl → selectedLectures |
| ScheduleController | request/response 타입 반영 |
| ScheduleSwagger | API 문서 업데이트 |

## 변경하지 않는 것

- EverytimeApi, EverytimeLecture, Schedule/Alarm/Place 엔티티
- ScheduleService.createEverytimeSchedule()
- 기존 예외 처리
