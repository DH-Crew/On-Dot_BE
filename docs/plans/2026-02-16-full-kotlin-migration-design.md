# DH-5: Full Kotlin Migration Design

## Goal

on-dot 프로젝트의 전체 Java 소스(297 main + 16 test = 313 files)를 idiomatic Kotlin으로 전환한다.

## Constraints

1. **비즈니스 플로우 보존 (CRITICAL)**: 운영 중인 코드이므로 기존 동작을 100% 유지해야 한다. 메서드 시그니처, 반환값, 예외 발생 조건, 이벤트 발행 타이밍 등 비즈니스 로직을 변경하지 않는다.
2. **코드 스타일**: `/Users/hs/IdeaProjects/baro` 프로젝트 패턴을 따른다.
3. **Java interop**: 전환 중 Java ↔ Kotlin 공존 상태에서 호환성을 보장한다.
4. **빌드 검증**: 각 레이어 전환 완료마다 `./gradlew clean build` 통과 필수.

## Reference Project Patterns (baro)

| 패턴 | baro 스타일 |
|------|------------|
| Entity | `class Entity(val ...) : BaseTimeEntity()` + companion factory |
| Service | `class Service(private val repo: Repo)` 생성자 주입 |
| DTO/Request/Response | `data class` |
| Logging | `private val log = LoggerFactory.getLogger(javaClass)` |
| Factory | `companion object { fun create(...) = Entity(...) }` |
| Enum | `enum class Foo(val value: String)` |

## Migration Phases

```
Phase 1: core (32 files)     — 기존 플랜으로 진행 중
Phase 2: member (97 files)   — core 완료 후 단독 전환
Phase 3: notification (21 files) + schedule (145 files) — member 완료 후 병렬 전환
Phase 4: cleanup             — Lombok 제거, Java 디렉토리 정리
```

## Phase 2: member 도메인 (97 files)

전환 순서: domain → core(exceptions) → application → api → infra → tests

### 의존성 주의사항
- `Member.java`가 schedule 도메인의 `AlarmMode`, `Snooze`, `Sound` import → schedule은 아직 Java이므로 Kotlin에서 Java class 참조 (자연 호환)
- `MemberFacade.java`가 `ScheduleService` import → 동일하게 Java 참조

## Phase 3: notification + schedule 병렬

### Agent 1 — notification (21 files)
- domain/ (11 files) → infra/ (10 files)
- notification → member 참조 2건 (`OauthProvider`, `UserRegistrationEvent`) — member가 이미 Kotlin이므로 Kotlin→Kotlin 호환

### Agent 2 — schedule (145 files)
- core/exception (9 files) → domain/ (54 files) → application/ (15 files) → api/ (36 files) → infra/ (31 files) → tests (9 files)

## Phase 4: Cleanup
- gradle/core.gradle에서 Lombok 임시 의존성 제거
- `kapt.keepJavacAnnotationProcessors = true` 제거
- 빈 Java 디렉토리 삭제
- 최종 빌드 + 전체 테스트 통과 확인

## 비즈니스 플로우 보존 체크리스트

각 파일 전환 시 반드시 확인:
- [ ] 원본 Java 파일의 모든 public 메서드가 Kotlin에 동일하게 존재
- [ ] 메서드 파라미터 타입, 반환 타입, nullable 여부 일치
- [ ] 예외 발생 조건과 타입 동일
- [ ] Spring annotation (@Transactional, @Async, @EventListener 등) 동일 적용
- [ ] JPA mapping (컬럼명, 관계, fetch 전략) 변경 없음
- [ ] `@JvmStatic`, `@JvmField` 등 Java interop 어노테이션 필요 여부 확인
- [ ] 빌드 + 테스트 통과
