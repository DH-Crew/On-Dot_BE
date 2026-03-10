# DH-89: 대중교통 시간 API TMAP으로 변경 - 설계 문서

## 개요

대중교통 예상 시간 계산을 ODSAY API에서 TMAP 대중교통 요약정보 API로 변경한다.
기존 ODSAY 코드는 삭제하지 않고 유지하며, 새로운 `TmapTransitRouteTimeCalculator`를 추가하여 Strategy 패턴으로 교체한다.

## 결정 사항

| 항목 | 결정 |
|------|------|
| API | `POST https://apis.openapi.sk.com/transit/routes/sub` (대중교통 요약정보) |
| appKey | 기존 TMAP 자가용과 동일한 appKey 사용 |
| 알고리즘 | 상위 3개 경로 totalTime 평균 + 5분 버퍼 (패널티/1.07x 비율 제거) |
| 시간 단위 | 응답 totalTime은 초 단위 → 분 변환 |
| 사용량 추적 | `ApiType.TMAP_TRANSIT` 추가, 기존 TMAP CAR과 동일 limit |
| ODSAY 코드 | 삭제하지 않고 유지 |
| 에러 11~14 | 도보 폴백 (직선거리 / 1.25 m/s) |
| 에러 31 | 재시도 트리거 |
| 에러 21, 22, 23, 32 | 사용자 에러 반환 |

## API 스펙

### 요청

```
POST https://apis.openapi.sk.com/transit/routes/sub
Headers:
  appKey: {tmap.app-key}
  Content-Type: application/json
  Accept: application/json
```

```json
{
  "startX": "127.025509",
  "startY": "37.637885",
  "endX": "127.030406",
  "endY": "37.609094",
  "count": 10,
  "lang": 0,
  "format": "json"
}
```

### 정상 응답

```json
{
  "metaData": {
    "requestParameters": {
      "startX": "127.025509",
      "startY": "37.637885",
      "endX": "127.030406",
      "endY": "37.609094"
    },
    "plan": {
      "itineraries": [
        {
          "totalTime": 984,
          "transferCount": 0,
          "walkDistance": 264.0,
          "walkTime": 274,
          "totalDistance": 4528.0,
          "pathType": 2
        }
      ]
    }
  }
}
```

### 에러 응답 (HTTP 200)

```json
{
  "result": {
    "status": 11,
    "message": "출발지/도착지 간 거리가 가까워서 탐색된 경로 없음"
  }
}
```

### 에러 코드

| 코드 | 의미 | HTTP | 처리 |
|------|------|------|------|
| 11 | 출발지/도착지 거리 가까움 | 200 | 도보 폴백 |
| 12 | 출발지 정류장 매핑 실패 | 200 | 도보 폴백 |
| 13 | 도착지 정류장 매핑 실패 | 200 | 도보 폴백 |
| 14 | 대중교통 경로 없음 | 200 | 도보 폴백 |
| 21 | 입력값 형식/범위 오류 | 400 | 에러 반환 |
| 22 | 필수 입력값 누락 | 400 | 에러 반환 |
| 23 | 서비스 지역 아님 | 400 | 에러 반환 |
| 31 | 서버 타임아웃 | 500 | 재시도 |
| 32 | 기타 오류 | 500 | 에러 반환 |

## 아키텍처

### 새로 생성하는 파일

```
schedule/infra/
├── TmapTransitRouteTimeCalculator.kt    # RouteTimeCalculator 구현
├── api/
│   └── TmapTransitPathApi.kt            # /transit/routes/sub 호출
├── dto/
│   ├── TmapTransitRouteApiResponse.kt   # 정상 응답 DTO
│   └── TmapTransitErrorResponse.kt      # 에러 응답 DTO
└── exception/
    ├── TmapTransitNoRouteException.kt   # 코드 11~14 (도보 폴백)
    ├── TmapTransitBadInputException.kt  # 코드 21
    ├── TmapTransitMissingParamException.kt  # 코드 22
    ├── TmapTransitServiceAreaException.kt   # 코드 23
    ├── TmapTransitServerErrorException.kt   # 코드 31 (재시도)
    └── TmapTransitUnhandledException.kt     # 코드 32 및 기타
```

### 수정하는 파일

```
schedule/domain/enums/ApiType.kt         # TMAP_TRANSIT 추가
core/exception/ErrorCode.kt             # TMAP_TRANSIT 에러 코드 추가
```

### 유지하는 파일 (변경 없음)

```
schedule/infra/OdsayRouteTimeCalculator.kt
schedule/infra/api/OdsayPathApi.kt
schedule/infra/dto/OdsayRouteApiResponse.kt
schedule/infra/exception/Odsay*.kt
core/config/OdsayApiConfig.kt
core/config/OdsayRestClientConfig.kt
```

## 시간 계산 알고리즘

```
1. ApiUsageService.checkUsage(TMAP_TRANSIT)
2. TmapTransitPathApi.searchTransitRoute(startLon, startLat, endLon, endLat)
   - count=10으로 요청
   - 에러 11~14 catch → 직선거리/1.25 m/s → 분 올림 반환
3. itineraries.map { it.totalTime / 60.0 } → 분 변환
4. 오름차순 정렬 → 상위 3개 (3개 미만이면 전체)
5. 평균 + 5분
6. ceil() → 정수 분 반환
```

### ODSAY 대비 변경점

| | ODSAY | TMAP Transit |
|---|---|---|
| 환승 패널티 | (transferCount - 1) × 6.5분 | 없음 |
| 장거리 도보 패널티 | 800m 초과 도보당 4.0분 | 없음 |
| 비율 버퍼 | × 1.07 | 없음 |
| 고정 버퍼 | + 5분 | + 5분 |
| 시간 단위 | 분 (API 응답) | 초 → 분 변환 |

## Strategy 패턴 교체

`RouteTimeCalculator` 인터페이스의 `transportType` 프로퍼티를 `PUBLIC_TRANSPORT`로 설정하면 `RouteService`에서 자동으로 선택된다.

기존 `OdsayRouteTimeCalculator`의 `transportType`이 `PUBLIC_TRANSPORT`이므로, 새 `TmapTransitRouteTimeCalculator`가 같은 값을 가지면 충돌이 발생한다. 이를 해결하기 위해:

- `OdsayRouteTimeCalculator`에서 `@Component`를 제거하거나 `@ConditionalOnProperty`로 비활성화
- 또는 `@Primary`를 `TmapTransitRouteTimeCalculator`에 부여

→ `@Primary` 방식 채택: ODSAY 코드를 최소한으로 수정하면서 TMAP을 우선 사용

## 사용량 제한

- `ApiType.TMAP_TRANSIT` 추가
- 기존 TMAP CAR과 동일한 일일 limit 설정
- Free 플랜 일 10건이나, Premium 전환 예정

## 요금 참고

| API | Free | Premium |
|-----|------|---------|
| 대중교통 요약정보 (`/transit/routes/sub`) | 10건/일 | 0.55원/건 |
| 대중교통 (`/transit/routes`) | 10건/일 | 0.88원/건 |
| TMAP 자동차 경로안내 | 1,000건/일 | 종량제 |
