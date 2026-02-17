# DH-30: 데일리 리마인더 기능 설계

## 개요

매일 KST 22:00에 다음 날 일정이 있는 사용자에게 푸시 알림을 전송하는 기능.

- 내일 일정이 없는 사용자에게는 보내지 않음
- iOS/Android 모두 지원 (FCM 단일 API)
- 하나의 계정으로 여러 기기 로그인 시 모든 기기에 전송
- 리마인더 시간: KST 22:00 고정 (추후 개인화 확장 가능)

## 결정 사항

| 항목 | 결정 | 근거 |
|------|------|------|
| 푸시 서비스 | FCM (Firebase Cloud Messaging) | iOS/Android 단일 API 지원 |
| 스케줄링 방식 | Spring @Scheduled (cron) | 기존 인프라 활용, 단순, 현재 단일 서버 |
| 시간 설정 | KST 22:00 고정 | 추후 개인화 확장 가능하게 설계 |
| 멀티 디바이스 | 모든 기기에 전송 | 가장 일반적인 방식 |
| 알림 설정 | Member.dailyReminderEnabled 필드 | 간단, 추후 분리 가능 |
| 메시지 형태 | 건수만 표시 | "내일 N개의 일정이 예정되어 있어요" |

## 엔티티 & DB 스키마

### DeviceToken (신규)

```
device_tokens 테이블
├── id: Long (PK, auto-increment)
├── member_id: Long (FK → members)
├── fcm_token: String (unique)
├── device_type: String ("iOS" / "Android")
├── created_at: Instant
└── updated_at: Instant
```

- 회원 1:N 디바이스 관계
- FCM 토큰 갱신 시 upsert
- 로그아웃 시 해당 토큰 삭제

### Member 변경

```
members 테이블 필드 추가:
└── daily_reminder_enabled: Boolean (default: true)
```

## API 엔드포인트

### 디바이스 토큰 관리

```
POST   /device-tokens  — FCM 토큰 등록/갱신
DELETE /device-tokens   — 토큰 삭제 (로그아웃 시)
```

**POST /device-tokens** (앱 실행 시 호출)
```json
{
  "fcmToken": "abc123...",
  "deviceType": "iOS"
}
→ 201 Created (신규) 또는 200 OK (갱신)
```

**DELETE /device-tokens** (로그아웃 시 호출)
```json
{
  "fcmToken": "abc123..."
}
→ 204 No Content
```

### 데일리 리마인더 설정

```
PATCH /members/me/daily-reminder
```

```json
{
  "enabled": true
}
→ 200 OK
```

리마인더 설정은 Member의 개인 설정이므로 member 모듈에 위치.

## 스케줄러 로직

### 실행 흐름

```
매일 KST 22:00 (@Scheduled cron)
│
├─ 1. 내일 일정이 있는 회원 조회
│     - 단발성: appointmentAt이 내일 날짜에 해당
│     - 반복: isRepeat=true && repeatDays에 내일 요일 포함
│     - dailyReminderEnabled=true인 회원만
│
├─ 2. 회원별 일정 건수 집계 → Map<memberId, count>
│
├─ 3. 해당 회원들의 디바이스 토큰 조회
│
└─ 4. FCM 전송 (비동기)
      - 메시지: "내일 {N}개의 일정이 예정되어 있어요"
      - 실패한 토큰 로그 기록
      - UNREGISTERED 응답 시 토큰 자동 삭제
```

## 패키지 구조

```
notification/
├── domain/
│   ├── DeviceToken.kt
│   ├── DeviceTokenRepository.kt
│   └── service/
│       └── DeviceTokenService.kt         # 토큰 CRUD (순수 도메인)
├── application/
│   ├── DeviceTokenFacade.kt              # 토큰 등록/삭제 오케스트레이션
│   ├── DailyReminderScheduler.kt         # @Scheduled 배치 (Facade 역할)
│   └── dto/
│       └── RegisterDeviceTokenCommand.kt
├── infra/
│   └── fcm/
│       ├── FcmConfig.kt                  # Firebase 초기화
│       └── FcmClient.kt                  # FCM API 호출
└── presentation/
    ├── DeviceTokenController.kt
    └── dto/

member/
├── domain/
│   └── Member.kt                         # dailyReminderEnabled 추가
│   └── service/
│       └── MemberService.kt              # 리마인더 설정 변경 메서드 추가
├── application/
│   └── MemberFacade.kt                   # 리마인더 토글 추가
└── presentation/
    └── MemberController.kt               # PATCH /members/me/daily-reminder 추가
```

### DailyReminderScheduler 의존성

```
DailyReminderScheduler (Facade 역할)
├── MemberService         → 리마인더 활성 회원 조회
├── ScheduleQueryService  → 내일 일정 조회
├── DeviceTokenService    → 디바이스 토큰 조회
└── FcmClient             → 푸시 전송 (외부 클라이언트, Facade에서 호출)
```

## 의존성 추가

```kotlin
// build.gradle.kts
implementation("com.google.firebase:firebase-admin:9.x.x")
```

Firebase 서비스 계정 키(JSON)는 환경변수 또는 resources에서 관리.

## 프론트엔드 팀 전달사항

1. 앱 실행/로그인 시 → FCM 토큰을 서버에 등록 (`POST /device-tokens`)
2. 로그아웃 시 → 토큰 삭제 (`DELETE /device-tokens`)
3. FCM 토큰 갱신 콜백 발생 시 → 재등록
4. 설정 화면에서 데일리 리마인더 on/off 토글 UI 필요
5. 푸시 알림 수신 시 앱 내 처리 (알림 탭, 딥링크 등)
