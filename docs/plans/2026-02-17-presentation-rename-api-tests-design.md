# Presentation 패키지 리네이밍 + API 테스트 디자인

## 목표

1. `api` 패키지를 `presentation`으로 rename
2. 전체 27개 엔드포인트에 대한 `@WebMvcTest` 슬라이스 테스트 작성

## 패키지 리네이밍

### 대상
- `member/api/` → `member/presentation/`
- `schedule/api/` → `schedule/presentation/`
- 하위 구조(`request/`, `response/`, `swagger/`, controllers) 유지
- `schedule/infra/api/`는 외부 API 호출이므로 유지

### 영향 범위
- ~32개 파일: package 선언 + import 경로 변경
- 컨트롤러 5개, swagger 5개, request/response ~22개

## 테스트 전략

### 방식
- `@WebMvcTest` 슬라이스 테스트
- Facade를 `@MockBean`으로 Mocking
- `TokenInterceptor`를 `@MockBean`으로 처리

### 인증
- 정상: `request.setAttribute("memberId", 1L)` stubbing
- 실패: 토큰 미제공 시 401 검증

### 테스트 파일 구조
```
src/test/kotlin/com/dh/ondot/member/presentation/
├── AuthControllerTest.kt        (4 endpoints)
└── MemberControllerTest.kt      (7 endpoints)

src/test/kotlin/com/dh/ondot/schedule/presentation/
├── ScheduleControllerTest.kt    (12 endpoints)
├── AlarmControllerTest.kt       (2 endpoints)
└── PlaceControllerTest.kt       (4 endpoints)
```

### 엔드포인트별 케이스
- Happy Path (200/201)
- Validation 실패 (400)
- 리소스 미존재 (404)
- 인증 실패 (401)

### 검증 항목
- HTTP 상태 코드
- 응답 JSON 구조 (jsonPath)
- Facade 메서드 호출 여부 (verify)
