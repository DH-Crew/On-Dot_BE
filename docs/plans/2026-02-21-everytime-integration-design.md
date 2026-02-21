# 에브리타임(에타) 연동 설계

**Issue**: [DH-20](https://linear.app/dh-crew/issue/DH-20)
**Date**: 2026-02-21
**Status**: Approved

## 개요

에브리타임 시간표 공유 URL을 기반으로 요일별 첫 수업 시작시간에 맞춰 반복 스케줄과 알람을 자동 생성하는 기능.

## API 엔드포인트

### 1) 에타 URL 검증

```
POST /schedules/everytime/validate
```

**Request:**
```json
{
  "everytimeUrl": "https://everytime.kr/@abc123"
}
```

**Response (200):**
```json
{
  "identifier": "abc123",
  "isValid": true
}
```

**에러 응답:**
- 400: URL 형식 오류
- 404: 존재하지 않는 identifier (빈 응답)
- 403: 비공개 시간표 (에브리타임 응답에서 판별 가능한 경우)
- 502: 에브리타임 서버 오류/타임아웃

### 2) 에타 기반 스케줄 생성

```
POST /schedules/everytime
```

**Request (SetAlarmRequest 네이밍 따름):**
```json
{
  "everytimeUrl": "https://everytime.kr/@abc123",
  "transportType": "PUBLIC_TRANSPORT",
  "startLongitude": 127.0276,
  "startLatitude": 37.4979,
  "endLongitude": 127.0437,
  "endLatitude": 37.5849
}
```

**Response (201):** 생성된 스케줄 목록

## 에브리타임 API 연동

### API 호출

```
POST https://api.everytime.kr/find/timetable/table/friend
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Origin: https://everytime.kr
Referer: https://everytime.kr/

identifier={id}&friendInfo=true
```

### URL → identifier 추출

`https://everytime.kr/@abc123` → `abc123`

### XML 응답 구조

```xml
<response>
  <subject>
    <name value="데이터구조"/>
    <professor value="홍길동"/>
    <time>
      <data day="0" starttime="114" endtime="120" place="공학관 301"/>
    </time>
  </subject>
  ...
</response>
```

- `day`: 0=월, 1=화, 2=수, 3=목, 4=금, 5=토, 6=일
- `starttime/endtime`: 5분 단위 인덱스 (예: 114 × 5 = 570분 = 09:30)

## 비즈니스 로직

### 처리 흐름

```
1. 에브리타임 API 호출 → XML 파싱
2. 요일별 첫 수업 시작시간 추출
3. 동일 시간 요일 그룹핑
4. 그룹별 경로 계산
5. 그룹별 Schedule + Alarm 생성 (일괄 저장)
```

### 요일별 첫 수업 추출

각 요일(월~일)에서 starttime이 가장 작은(이른) 수업의 시작시간을 선택.

### 동일 시간 그룹핑 & 스케줄 이름

동일 시작시간의 요일을 하나의 스케줄로 합침:
- 월/화/수 09:30 → "월/화/수요일 학교"
- 목 10:30 → "목요일 학교"
- 금 11:30 → "금요일 학교"

### 요일 매핑 (에브리타임 → DB)

| 에브리타임 day | 실제 요일 | DB repeatDays (1=일~7=토) |
|---|---|---|
| 0 | 월 | 2 |
| 1 | 화 | 3 |
| 2 | 수 | 4 |
| 3 | 목 | 5 |
| 4 | 금 | 6 |
| 5 | 토 | 7 |
| 6 | 일 | 1 |

### 경로 계산 전략

- **대중교통**: 1회 조회 후 전체 그룹에 재사용
- **자가용**: 시간대별 조회 필요. 동일 시간 그룹 중 첫 번째 요일(월→화→...→일 순)로 TMAP 조회, 같은 시간대 나머지 요일은 결과 재사용

### appointmentAt 계산

반복 스케줄이므로 그룹 내 첫 번째 요일(월→일 순)의 가장 가까운 미래 날짜 + 시작시간을 appointmentAt으로 설정.

### 알람 설정

Member 기본 설정 사용 (기존 Quick Schedule 패턴):
- `defaultAlarmMode`, `snooze`, `sound`, `preparationTime`
- `departureAlarm.triggeredAt` = appointmentAt - 경로 소요시간
- `preparationAlarm.triggeredAt` = departureAlarm.triggeredAt - preparationTime

## 레이어별 컴포넌트

### Presentation (기존 파일에 추가)

- `ScheduleController.kt` — 에타 엔드포인트 2개 추가
- `request/EverytimeValidateRequest.kt` — 신규
- `request/EverytimeScheduleCreateRequest.kt` — 신규
- `response/EverytimeValidateResponse.kt` — 신규

### Application (기존 파일에 추가)

- `ScheduleCommandFacade.kt` — 메서드 2개 추가
  - `validateEverytimeUrl(url)`
  - `createSchedulesFromEverytime(memberId, command)`
- `command/CreateEverytimeScheduleCommand.kt` — 신규
- `dto/EverytimeLecture.kt` — 신규 (파싱된 수업 데이터)

### Infra (신규)

- `api/EverytimeApi.kt` — API 호출 + XML 파싱
- `config/EverytimeRestClientConfig.kt` — RestClient 빈 (connect 3s, read 5s)
- `exception/EverytimeInvalidUrlException.kt` — 400
- `exception/EverytimeNotFoundException.kt` — 404
- `exception/EverytimePrivateException.kt` — 403
- `exception/EverytimeServerException.kt` — 502

### Domain

기존 `Schedule`, `Alarm`, `Place` 엔티티를 그대로 사용. 신규 도메인 모델 추가 없음.

## 에러 처리 & 엣지 케이스

| 케이스 | 처리 |
|---|---|
| 수업이 없는 요일 | 해당 요일은 스케줄 생성하지 않음 |
| 시간표에 수업이 0개 | 에러 반환 (빈 시간표) |
| 에브리타임 서버 장애 | 502 + 재시도 (@Retryable, 2회) |
| API 사용량 한도 초과 | 기존 ApiUsageService 활용, 429 반환 |
| 동일 에타 URL로 중복 생성 | 별도 중복 체크 없이 생성 허용 (사용자 판단에 위임) |
