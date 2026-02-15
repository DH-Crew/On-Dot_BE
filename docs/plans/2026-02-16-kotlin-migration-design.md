# DH-5: Kotlin 전환 디자인

## 개요

On-Dot 백엔드 프로젝트를 Java에서 Kotlin으로 전환한다.
이번 티켓(DH-5)의 범위는 **빌드 설정 전환 + core 모듈 전환**이다.

- **참고 프로젝트**: `/Users/hs/IdeaProjects/baro` (같은 팀의 Kotlin Spring Boot 프로젝트)
- **접근 방식**: 빌드 설정을 Kotlin 대응으로 전환한 뒤, core 모듈 30개 파일을 idiomatic Kotlin으로 재작성

## 현재 상태

| 항목 | 값 |
|------|-----|
| 프레임워크 | Spring Boot 3.4.4 / Java 17 |
| 빌드 | Gradle (Groovy DSL) |
| 파일 수 | 261개 (main 245 + test 16) |
| 모듈 | schedule(114), member(79), core(30), notification(21) |
| 주요 의존성 | JPA/QueryDsl, MapStruct, Lombok, Redis, Kafka, Spring AI |
| Kotlin 코드 | 전무 |

## 1. 빌드 설정 전환

### 1-1. build.gradle 변경

**Kotlin 플러그인 추가**:
```groovy
plugins {
    id "org.jetbrains.kotlin.jvm" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.plugin.spring" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.plugin.jpa" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.kapt" version "${jetbrainKotlinVersion}"
    id "org.springframework.boot" version "${springbootVersion}"
    id "io.spring.dependency-management" version "${springDependencyManagementVersion}"
}
```

**allOpen 블록** (baro 패턴):
```groovy
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

**java 플러그인 제거**, `java` 블록은 toolchain 설정으로 유지.

### 1-2. gradle.properties 버전 관리

baro 패턴에 따라 `gradle.properties`에 버전 변수 추가:
```properties
### Jetbrain ###
jetbrainKotlinVersion=1.9.22
jvmTarget=17

### Spring ###
springbootVersion=3.4.4
springDependencyManagementVersion=1.1.7
```

### 1-3. gradle 서브파일 분리

```
gradle/
├── core.gradle        — jackson-module-kotlin, BouncyCastle 등 공통 라이브러리
├── db.gradle          — MySQL connector, H2, Redis
├── jetbrains.gradle   — Kotlin 컴파일러 설정, kotlin-reflect, coroutines
├── spring.gradle      — Spring Boot starters, springdoc, p6spy, kafka, spring-ai, spring-retry
├── monitor.gradle     — actuator, micrometer-prometheus
├── test.gradle        — JUnit, TestContainers
```

**build.gradle**에서 `apply from:` 으로 참조:
```groovy
apply from: "gradle/core.gradle"
apply from: "gradle/db.gradle"
apply from: "gradle/jetbrains.gradle"
apply from: "gradle/monitor.gradle"
apply from: "gradle/spring.gradle"
apply from: "gradle/test.gradle"
```

### 1-4. 의존성 변경 사항

| 변경 | 상세 |
|------|------|
| 추가 | `kotlin-reflect`, `kotlinx-coroutines-reactor`, `kotlinx-coroutines-slf4j`, `jackson-module-kotlin` |
| 전환 | `annotationProcessor` -> `kapt` (MapStruct, QueryDsl) |
| 제거 | Lombok (`compileOnly`, `annotationProcessor`, `lombok-mapstruct-binding`) |
| 유지 | MapStruct (kapt로 전환), QueryDsl (kapt로 전환), JWT, Spring AI |

### 1-5. 소스 디렉토리

- `src/main/kotlin/com/dh/ondot/` 생성
- core 모듈의 Kotlin 파일을 여기에 배치
- 나머지 Java 모듈은 `src/main/java/` 에 그대로 유지 (Java/Kotlin 공존)

## 2. Core 모듈 전환 (30개 파일)

### 2-1. 어노테이션/마커

| Java | Kotlin |
|------|--------|
| `@AggregateRoot` interface | `annotation class AggregateRoot` |
| `BaseTimeEntity` abstract + Lombok | `abstract class BaseTimeEntity(var createdAt, var modifiedAt) : Persistable<Long>` |

### 2-2. Config 클래스 (12개)

baro 패턴 적용:
- `@RequiredArgsConstructor` -> 생성자 주입 `class XConfig(private val ...)`
- `@Value` 필드 -> `@Value` + `lateinit var` 또는 생성자 주입
- static 상수 -> `companion object { const val ... }`
- `@Slf4j` -> `LoggerFactory.getLogger(javaClass)`

대상: AppConfig, AsyncConfig, AsyncProperties, QueryDslConfig, WebConfig, SpringAiConfig, OpenApiConfig, P6SpyFormatter, OauthProviderConverter, TokenInterceptor, KafkaConfig, RedisConfig

### 2-3. Exception 클래스 (12개)

| Java | Kotlin |
|------|--------|
| `abstract class NotFoundException extends RuntimeException` | `abstract class NotFoundException(message: String) : RuntimeException(message)` |
| `ErrorCode` enum + Lombok | `enum class ErrorCode(val status: HttpStatus, val message: String)` |
| `ErrorResponse` | `data class ErrorResponse(...)` + `companion object` 팩토리 |
| `GlobalExceptionHandler` | `class GlobalExceptionHandler` + `@RestControllerAdvice` |

### 2-4. Utility 클래스 (2개)

| Java | Kotlin |
|------|--------|
| `@UtilityClass TimeUtils` | 최상위 함수 + 확장 함수 (baro의 `Time.kt` 패턴) |
| `GeoUtils` | `object GeoUtils` 또는 최상위 함수 |

### 2-5. 기타

| Java | Kotlin |
|------|--------|
| `TokenInterceptor` | `class TokenInterceptor(...) : HandlerInterceptor` |
| `OauthProviderConverter` | `class OauthProviderConverter : Converter<String, OauthProvider>` |

## 3. Java-Kotlin Interop 전략

core만 Kotlin으로 전환하므로, Java 모듈(member, schedule, notification)에서 core를 참조할 때 호환성을 보장해야 한다.

- `allOpen` 플러그인으로 Spring 관련 클래스 자동 open 처리
- `companion object` 팩토리 메서드에 `@JvmStatic` 추가 (Java에서 직접 호출하는 경우)
- `companion object` 상수에 `@JvmField` 추가 (Java에서 직접 참조하는 경우)
- `ErrorResponse`, `ErrorCode` 등 Java에서 직접 참조하는 클래스는 Java 호환성 검증 필수
- Kotlin `data class`의 `copy()` 는 Java에서 사용 불가하므로, Java 측에서 필요한 경우 명시적 메서드 제공

## 4. 범위 외 (향후 티켓)

- member, schedule, notification 모듈 전환
- Coroutines 전환 (Spring MVC -> suspend fun)
- Kotlin JDSL 도입 (QueryDsl 대체)
- Kotest 도입 (JUnit 대체)
- Gradle Kotlin DSL 전환 (build.gradle -> build.gradle.kts)
