# DH-5: Kotlin 전환 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 빌드 설정을 Kotlin 대응으로 전환하고, core 모듈 30개 Java 파일을 idiomatic Kotlin으로 재작성한다.

**Architecture:** baro 프로젝트(`/Users/hs/IdeaProjects/baro`) 패턴을 따른다. Gradle Groovy DSL 유지 + Kotlin 플러그인 추가, gradle 서브파일 분리, core 모듈을 `src/main/kotlin`으로 이동. Java/Kotlin 공존 상태에서 Java 모듈이 Kotlin core를 참조할 수 있도록 interop 보장.

**Tech Stack:** Kotlin 1.9.22, Spring Boot 3.4.4, Gradle (Groovy DSL), kapt, allOpen/noArg plugins

**Verify command:** `./gradlew clean build` (빌드 + 테스트 전체 실행)

**Reference project:** `/Users/hs/IdeaProjects/baro` — 동일 팀의 Kotlin Spring Boot 프로젝트

---

## Task 1: gradle.properties 생성

**Files:**
- Create: `gradle.properties`

**Step 1: gradle.properties 작성**

```properties
kotlin.code.style=official

### Project ###
group=com.dh
version=0.0.1-SNAPSHOT

### Spring ###
springbootVersion=3.4.4
springDependencyManagementVersion=1.1.7

### Jetbrain ###
jetbrainKotlinVersion=1.9.22
jvmTarget=17

### Swagger ###
springdocVersion=2.6.0

### p6spy ###
p6spyVersion=1.9.0

### MapStruct ###
mapstructVersion=1.5.5.Final

### QueryDsl ###
querydslVersion=5.0.0

### JWT ###
jwtVersion=0.12.5

### BouncyCastle ###
bouncycastleVersion=1.76

### TestContainer ###
testContainerVersion=2.0.3

### Spring AI ###
springAiVersion=1.0.0-SNAPSHOT

### Jackson ###
jacksonVersion=2.18.3
```

**Step 2: 커밋**

```bash
git add gradle.properties
git commit -m "build: gradle.properties 버전 변수 추가"
```

---

## Task 2: gradle 서브파일 분리

**Files:**
- Create: `gradle/core.gradle`
- Create: `gradle/db.gradle`
- Create: `gradle/jetbrains.gradle`
- Create: `gradle/spring.gradle`
- Create: `gradle/monitor.gradle`
- Create: `gradle/test.gradle`

**Step 1: gradle/jetbrains.gradle 작성**

```groovy
compileKotlin {
    kotlinOptions.jvmTarget = "${jvmTarget}"
}

compileTestKotlin {
    kotlinOptions.jvmTarget = "${jvmTarget}"
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-reflect"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-slf4j"

    testImplementation "org.jetbrains.kotlin:kotlin-test"
}
```

**Step 2: gradle/core.gradle 작성**

```groovy
dependencies {
    implementation "com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}"

    // BouncyCastle
    implementation "org.bouncycastle:bcprov-jdk15to18:${bouncycastleVersion}"
    implementation "org.bouncycastle:bcpkix-jdk15to18:${bouncycastleVersion}"

    // JWT
    implementation "io.jsonwebtoken:jjwt-api:${jwtVersion}"
    implementation "io.jsonwebtoken:jjwt-impl:${jwtVersion}"
    implementation "io.jsonwebtoken:jjwt-jackson:${jwtVersion}"

    // MapStruct
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    kapt "org.mapstruct:mapstruct-processor:${mapstructVersion}"

    // QueryDsl
    implementation "com.querydsl:querydsl-jpa:${querydslVersion}:jakarta"
    kapt "com.querydsl:querydsl-apt:${querydslVersion}:jakarta"
    kapt "jakarta.annotation:jakarta.annotation-api"
    kapt "jakarta.persistence:jakarta.persistence-api"
}
```

**Step 3: gradle/db.gradle 작성**

```groovy
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-data-jpa"
    implementation "org.springframework.boot:spring-boot-starter-data-redis"
    runtimeOnly "com.mysql:mysql-connector-j"

    testRuntimeOnly "com.h2database:h2"
}
```

**Step 4: gradle/spring.gradle 작성**

```groovy
jar {
    enabled = false
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web"
    implementation "org.springframework.boot:spring-boot-starter-validation"
    implementation "org.springframework.retry:spring-retry"
    implementation "org.springframework:spring-aspects"
    implementation "org.springframework.kafka:spring-kafka"
    implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}"
    implementation "com.github.gavlyukovskiy:p6spy-spring-boot-starter:${p6spyVersion}"
    developmentOnly "org.springframework.boot:spring-boot-devtools"

    // Spring AI
    implementation platform("org.springframework.ai:spring-ai-bom:${springAiVersion}")
    implementation "org.springframework.ai:spring-ai-openai:${springAiVersion}"
    implementation "org.springframework.ai:spring-ai-starter-model-openai:${springAiVersion}"
}
```

**Step 5: gradle/monitor.gradle 작성**

```groovy
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-actuator"
    runtimeOnly "io.micrometer:micrometer-registry-prometheus"
}
```

**Step 6: gradle/test.gradle 작성**

```groovy
test {
    useJUnitPlatform()
}

dependencies {
    testImplementation "org.springframework.boot:spring-boot-starter-test"
    testImplementation "org.springframework.kafka:spring-kafka-test"

    testImplementation "org.testcontainers:testcontainers:${testContainerVersion}"
    testImplementation "org.testcontainers:testcontainers-junit-jupiter:${testContainerVersion}"
    testImplementation "org.testcontainers:testcontainers-mysql:${testContainerVersion}"
}
```

**Step 7: 커밋**

```bash
git add gradle/
git commit -m "build: gradle 서브파일 분리 (core, db, jetbrains, spring, monitor, test)"
```

---

## Task 3: build.gradle 전환

**Files:**
- Modify: `build.gradle` (전체 재작성)

**Step 1: build.gradle을 Kotlin 플러그인 + 서브파일 참조로 재작성**

```groovy
plugins {
    id "org.jetbrains.kotlin.jvm" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.plugin.spring" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.plugin.jpa" version "${jetbrainKotlinVersion}"
    id "org.jetbrains.kotlin.kapt" version "${jetbrainKotlinVersion}"
    id "org.springframework.boot" version "${springbootVersion}"
    id "io.spring.dependency-management" version "${springDependencyManagementVersion}"
}

group = "${group}"
version = "${version}"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmTarget)
    }
}

repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
    maven { url 'https://repo.spring.io/snapshot' }
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${springAiVersion}")
    }
}

apply from: "gradle/core.gradle"
apply from: "gradle/db.gradle"
apply from: "gradle/jetbrains.gradle"
apply from: "gradle/monitor.gradle"
apply from: "gradle/spring.gradle"
apply from: "gradle/test.gradle"
```

**주의:** `java` 플러그인 제거, Lombok 의존성 제거 (`compileOnly`, `annotationProcessor`), `annotationProcessor` → `kapt` 전환은 gradle/core.gradle에서 처리됨, `tasks.named('test')` 는 gradle/test.gradle에서 처리됨

**Step 2: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL (Java 코드는 그대로이므로 Lombok 제거 전까지는 기존 코드가 깨질 수 있음)

**중요:** 이 시점에서 Lombok을 사용하는 Java 코드가 빌드에 실패할 수 있다. Lombok 제거는 core 모듈 Kotlin 전환과 동시에 진행되므로, `compileOnly 'org.projectlombok:lombok'`과 `annotationProcessor 'org.projectlombok:lombok'`은 **일시적으로 유지**해야 한다. gradle/core.gradle에 Lombok 의존성을 임시 추가한다:

gradle/core.gradle에 추가:
```groovy
// TODO: Kotlin 전환 완료 후 제거
compileOnly "org.projectlombok:lombok"
annotationProcessor "org.projectlombok:lombok"
```

**Step 3: 빌드 재검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```bash
git add build.gradle gradle/core.gradle
git commit -m "build: Kotlin 플러그인 추가 및 build.gradle 서브파일 참조로 전환"
```

---

## Task 4: Kotlin 소스 디렉토리 생성 + Application 클래스 검증

**Files:**
- Create: `src/main/kotlin/com/dh/ondot/` (디렉토리)
- Create: `src/test/kotlin/com/dh/ondot/` (디렉토리)

**Step 1: 디렉토리 생성**

```bash
mkdir -p src/main/kotlin/com/dh/ondot/core
mkdir -p src/test/kotlin/com/dh/ondot/core
```

**Step 2: 빌드 검증** — Kotlin 소스셋이 인식되는지 확인

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 3: 커밋**

```bash
git commit --allow-empty -m "build: Kotlin 소스 디렉토리 구조 생성"
```

---

## Task 5: core 어노테이션/마커 전환 (AggregateRoot, BaseTimeEntity)

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/AggregateRoot.java`
- Delete: `src/main/java/com/dh/ondot/core/BaseTimeEntity.java`
- Create: `src/main/kotlin/com/dh/ondot/core/AggregateRoot.kt`
- Create: `src/main/kotlin/com/dh/ondot/core/BaseTimeEntity.kt`

**Step 1: AggregateRoot.kt 작성**

```kotlin
package com.dh.ondot.core

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AggregateRoot
```

**Step 2: BaseTimeEntity.kt 작성**

baro 패턴 참고 (`/Users/hs/IdeaProjects/baro/src/main/kotlin/com/dh/baro/core/BaseTimeEntity.kt`):

```kotlin
package com.dh.ondot.core

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity(
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,
)
```

**주의:** on-dot은 `updatedAt` 필드명을 사용하고, baro는 `modifiedAt`을 사용한다. on-dot 기존 컬럼명 유지. 또한 on-dot의 BaseTimeEntity는 `Persistable<Long>`을 구현하지 않으므로 baro와 다르게 Persistable은 제외한다.

**Step 3: Java 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/AggregateRoot.java
rm src/main/java/com/dh/ondot/core/BaseTimeEntity.java
```

**Step 4: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL — Java 모듈의 `extends BaseTimeEntity` 가 Kotlin 클래스를 정상 참조

**Step 5: 커밋**

```bash
git add -A src/main/java/com/dh/ondot/core/AggregateRoot.java src/main/java/com/dh/ondot/core/BaseTimeEntity.java src/main/kotlin/com/dh/ondot/core/
git commit -m "refactor: AggregateRoot, BaseTimeEntity를 Kotlin으로 전환"
```

---

## Task 6: core exception 클래스 전환 (9개 추상 예외 + ErrorCode + UnsupportedException)

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/exception/` (12개 Java 파일 전부)
- Create: `src/main/kotlin/com/dh/ondot/core/exception/` (12개 Kotlin 파일)

**Step 1: 추상 예외 클래스 9개 작성** — 모두 동일 패턴이므로 한번에 작성

Create `src/main/kotlin/com/dh/ondot/core/exception/Exceptions.kt`:

```kotlin
package com.dh.ondot.core.exception

abstract class BadRequestException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class UnauthorizedException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ForbiddenException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class NotFoundException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ConflictException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class TooManyRequestsException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class InternalServerException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class BadGatewayException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}

abstract class ServiceUnavailableException(message: String) : RuntimeException(message) {
    abstract val errorCode: String
}
```

**주의 (Java interop):** Java 서브클래스에서 `getErrorCode()` 메서드를 오버라이드하고 있다. Kotlin의 `abstract val errorCode: String` 은 Java에서 `getErrorCode()` 를 생성하므로 호환된다. 단, Java 서브클래스가 `public abstract String getErrorCode();` 시그니처를 기대하므로, Kotlin에서 `abstract val` → Java에서 abstract getter로 보이는지 확인이 필요하다. Kotlin `abstract val`은 자동으로 `abstract getErrorCode()` 를 생성하므로 OK.

**Step 2: ErrorCode.kt 작성**

Create `src/main/kotlin/com/dh/ondot/core/exception/ErrorCode.kt`:

```kotlin
package com.dh.ondot.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*

enum class ErrorCode(
    @JvmField val httpStatus: HttpStatus,
    @JvmField val message: String,
) {
    // Common
    INVALID_JSON(BAD_REQUEST, "잘못된 JSON 형식입니다. 요청 데이터를 확인하세요."),
    FIELD_ERROR(BAD_REQUEST, "입력이 잘못되었습니다."),
    URL_PARAMETER_ERROR(BAD_REQUEST, "입력이 잘못되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(BAD_REQUEST, "입력한 값의 타입이 잘못되었습니다."),
    ALREADY_DISCONNECTED(BAD_REQUEST, "이미 클라이언트에서 요청이 종료되었습니다."),
    NO_RESOURCE_FOUND(NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_SUPPORTED(METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(UNSUPPORTED_MEDIA_TYPE, "허용되지 않은 미디어 타입입니다."),
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 관리자에게 문의해주세요."),
    REDIS_UNAVAILABLE(SERVICE_UNAVAILABLE, "Redis 서버에 연결할 수 없습니다."),
    EVENT_SERIALIZATION_FAILED(INTERNAL_SERVER_ERROR, "이벤트 직렬화 중 오류가 발생했습니다."),

    // Token
    INVALID_TOKEN_HEADER(UNAUTHORIZED, "토큰 헤더 형식이 잘못되었습니다."),
    TOKEN_INVALID(UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해 주세요."),
    TOKEN_MISSING(UNAUTHORIZED, "토큰이 요청 헤더에 없습니다. 새로운 토큰을 재발급 받으세요"),
    TOKEN_BLACKLISTED(UNAUTHORIZED, "해당 토큰은 사용이 금지되었습니다. 다시 로그인해 주세요."),
    TOKEN_EXPIRED(UNAUTHORIZED, "토큰이 만료되었습니다. 새로운 토큰을 재발급 받으세요."),
    REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, "리프레쉬 토큰이 만료되었습니다. 다시 로그인해 주세요."),

    // OAuth
    UNSUPPORTED_SOCIAL_LOGIN(BAD_REQUEST, "지원하지 않는 소셜 로그인 타입입니다. type : %s"),
    OAUTH_USER_FETCH_FAILED(SERVICE_UNAVAILABLE, "%s 사용자 정보를 가져오는 데 실패했습니다. 잠시 후 다시 시도해주세요."),
    APPLE_AUTHORIZATION_CODE_EXPIRED(UNAUTHORIZED, "애플 Authorization Code가 만료되었거나 잘못되었습니다. 다시 시도해주세요."),
    APPLE_SIGNATURE_INVALID(UNAUTHORIZED, "애플 id_token 서명 검증에 실패했습니다. 위조되었을 가능성이 있습니다."),
    APPLE_USER_PARSE_FAILED(NOT_FOUND, "애플 사용자 정보를 파싱하는 데 실패했습니다. id_token 구조를 확인하세요."),
    APPLE_PRIVATE_KEY_LOAD_FAILED(INTERNAL_SERVER_ERROR, "Apple 비공개 키 파일을 로드하는 데 실패했습니다. 파일 경로 또는 포맷을 확인하세요."),

    // Member
    NOT_FOUND_MEMBER(NOT_FOUND, "회원을 찾을 수 없습니다. MemberId : %d"),
    ALREADY_ONBOARDED_MEMBER(CONFLICT, "이미 온보딩을 완료한 회원입니다. MemberId : %d"),
    NOT_FOUND_QUESTION(NOT_FOUND, "질문을 찾을 수 없습니다. QuestionId : %d"),
    NOT_FOUND_ANSWER(NOT_FOUND, "답을 찾을 수 없습니다. AnswerId : %d"),
    NOT_FOUND_HOME_ADDRESS(NOT_FOUND, "회원이 저장한 주소를 찾을 수 없습니다. MemberId : %d"),
    UNSUPPORTED_MAP_PROVIDER(BAD_REQUEST, "지원하지 않는 지도 제공자입니다. MapProvider : %s"),
    UNSUPPORTED_ADDRESS_TYPE(BAD_REQUEST, "지원하지 않는 주소 타입입니다. AddressType : %s"),

    // Alarm
    UNSUPPORTED_ALARM_MODE(BAD_REQUEST, "지원하지 않는 알람 모드입니다. Mode : %s"),
    UNSUPPORTED_RING_TONE(BAD_REQUEST, "지원하지 않는 벨소리입니다. RingTone : %s"),
    UNSUPPORTED_SNOOZE_INTERVAL(BAD_REQUEST, "지원하지 않는 알람 미루기 간격입니다. SnoozeInterval : %s"),
    UNSUPPORTED_SNOOZE_COUNT(BAD_REQUEST, "지원하지 않는 알람 미루기 횟수입니다. SnoozeCount : %s"),
    UNSUPPORTED_SOUND_CATEGORY(BAD_REQUEST, "지원하지 않는 사운드 카테고리입니다. SoundCategory : %s"),
    UNSUPPORTED_MISSION(BAD_REQUEST, "지원하지 않는 미션입니다. Mission : %s"),
    NOT_FOUND_ALARM(NOT_FOUND, "알람을 찾을 수 없습니다. AlarmId : %d"),
    INVALID_ALARM_TRIGGER_ACTION(BAD_REQUEST, "지원하지 않는 알람 트리거 액션입니다. Action : %s"),
    NOT_FOUND_SCHEDULE(NOT_FOUND, "일정을 찾을 수 없습니다. ScheduleId : %d"),

    // Place
    PLACE_HISTORY_SERIALIZATION_FAILED(INTERNAL_SERVER_ERROR, "장소 검색 기록 직렬화 중 오류가 발생했습니다."),

    // AI
    AI_USAGE_LIMIT_EXCEEDED(TOO_MANY_REQUESTS, "오늘 사용 가능한 AI 사용 횟수를 초과했습니다. MemberId : %d, Date : %s"),
    OPEN_AI_PARSING_ERROR(BAD_REQUEST, "약속 문장을 이해할 수 없습니다. 형식을 확인 후 다시 시도해주세요."),
    UNAVAILABLE_OPEN_AI_SERVER(BAD_GATEWAY, "일시적으로 Open AI 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNHANDLED_OPEN_AI(INTERNAL_SERVER_ERROR, "Open AI 요청 과정에서 알 수 없는 문제가 발생했습니다. 관리자에게 문의해주세요."),

    // ODsay API
    ODSAY_BAD_INPUT(BAD_REQUEST, "필수 입력값 형식 및 범위를 확인해주세요: %s"),
    ODSAY_MISSING_PARAM(BAD_REQUEST, "필수 입력값이 누락되었습니다: %s"),
    ODSAY_NO_STOP(BAD_REQUEST, "출발지 또는 도착지 정류장을 찾을 수 없습니다: %s"),
    ODSAY_SERVICE_AREA(BAD_REQUEST, "서비스 지역이 아닙니다: %s"),
    ODSAY_TOO_CLOSE(BAD_REQUEST, "출발지와 도착지가 너무 가깝습니다: %s"),
    ODSAY_USAGE_LIMIT_EXCEEDED(FORBIDDEN, "오늘 Odsay API 사용 한도를 초과했습니다. Date : %s"),
    ODSAY_NO_RESULT(NOT_FOUND, "검색 결과가 없습니다: %s"),
    ODSAY_SERVER_ERROR(BAD_GATEWAY, "ODSay 서버 내부 오류가 발생했습니다: %s"),
    ODSAY_UNHANDLED_ERROR(INTERNAL_SERVER_ERROR, "ODSay API 처리 중 알 수 없는 오류가 발생했습니다: %s"),
    ;
}
```

**주의 (Java interop):** Java 코드에서 `errorCode.getHttpStatus()`, `errorCode.getMessage()` 로 접근한다. Kotlin enum에서 `val` 프로퍼티는 자동으로 getter를 생성하지만, Java에서 `errorCode.httpStatus` 처럼 **필드 직접 접근**이 필요한 코드가 있을 수 있다. 기존 Java 코드에서 `errorCode.httpStatus` (public final 필드 접근) 패턴이 있으므로 `@JvmField` 를 사용한다.

**Step 3: UnsupportedException.kt 작성**

Create `src/main/kotlin/com/dh/ondot/core/exception/UnsupportedException.kt`:

```kotlin
package com.dh.ondot.core.exception

class UnsupportedException(
    private val error: ErrorCode,
    obj: String,
) : BadRequestException(error.message.format(obj)) {
    override val errorCode: String get() = error.name
}
```

**Step 4: Java 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/exception/BadRequestException.java
rm src/main/java/com/dh/ondot/core/exception/UnauthorizedException.java
rm src/main/java/com/dh/ondot/core/exception/ForbiddenException.java
rm src/main/java/com/dh/ondot/core/exception/NotFoundException.java
rm src/main/java/com/dh/ondot/core/exception/ConflictException.java
rm src/main/java/com/dh/ondot/core/exception/TooManyRequestsException.java
rm src/main/java/com/dh/ondot/core/exception/InternalServerException.java
rm src/main/java/com/dh/ondot/core/exception/BadGatewayException.java
rm src/main/java/com/dh/ondot/core/exception/ServiceUnavailableException.java
rm src/main/java/com/dh/ondot/core/exception/ErrorCode.java
rm src/main/java/com/dh/ondot/core/exception/UnsupportedException.java
```

**Step 5: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL — Java 서브클래스가 Kotlin 추상 예외를 상속하고, `getErrorCode()` 메서드를 오버라이드. 실패 시 Java 서브클래스의 `getErrorCode()` 메서드 시그니처를 확인하고, Kotlin 추상 클래스에서 `abstract fun getErrorCode(): String` 으로 변경 검토.

**Step 6: 커밋**

```bash
git add -A
git commit -m "refactor: core exception 클래스를 Kotlin으로 전환"
```

---

## Task 7: ErrorResponse + GlobalExceptionHandler 전환

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/ErrorResponse.java`
- Delete: `src/main/java/com/dh/ondot/core/exception/GlobalExceptionHandler.java`
- Create: `src/main/kotlin/com/dh/ondot/core/ErrorResponse.kt`
- Create: `src/main/kotlin/com/dh/ondot/core/exception/GlobalExceptionHandler.kt`

**Step 1: ErrorResponse.kt 작성**

```kotlin
package com.dh.ondot.core

import com.dh.ondot.core.exception.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.ConstraintViolation
import org.springframework.validation.BindingResult

@Schema(description = "API 에러 응답")
@JsonInclude(Include.NON_NULL)
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "TODO_LIMIT_EXCEEDED")
    val errorCode: String,

    @Schema(description = "에러 메시지", example = "오늘 할 일은 최대 3개까지 추가할 수 있습니다.")
    val message: String,

    @Schema(description = "필드 오류 목록 (있을 경우)")
    val fieldErrors: List<FieldError>? = null,

    @Schema(description = "제약 조건 위반 오류 목록 (있을 경우)")
    val violationErrors: List<ConstraintViolationError>? = null,
) {

    constructor(e: BadRequestException) : this(e.errorCode, e.message ?: "")
    constructor(e: UnauthorizedException) : this(e.errorCode, e.message ?: "")
    constructor(e: ForbiddenException) : this(e.errorCode, e.message ?: "")
    constructor(e: NotFoundException) : this(e.errorCode, e.message ?: "")
    constructor(e: ConflictException) : this(e.errorCode, e.message ?: "")
    constructor(e: TooManyRequestsException) : this(e.errorCode, e.message ?: "")
    constructor(e: InternalServerException) : this(e.errorCode, e.message ?: "")
    constructor(e: BadGatewayException) : this(e.errorCode, e.message ?: "")
    constructor(e: ServiceUnavailableException) : this(e.errorCode, e.message ?: "")

    constructor(errorCode: ErrorCode) : this(errorCode.name, errorCode.message)

    constructor(errorCode: ErrorCode, bindingResult: BindingResult) : this(
        errorCode = errorCode.name,
        message = errorCode.message,
        fieldErrors = FieldError.from(bindingResult),
    )

    constructor(errorCode: ErrorCode, constraintViolations: Set<ConstraintViolation<*>>) : this(
        errorCode = errorCode.name,
        message = errorCode.message,
        violationErrors = ConstraintViolationError.from(constraintViolations),
    )

    data class FieldError(
        val field: String,
        val rejectedValue: Any?,
        val reason: String?,
    ) {
        companion object {
            @JvmStatic
            fun from(bindingResult: BindingResult): List<FieldError> =
                bindingResult.fieldErrors.map { e ->
                    FieldError(
                        field = e.field,
                        rejectedValue = e.rejectedValue,
                        reason = e.defaultMessage,
                    )
                }
        }
    }

    data class ConstraintViolationError(
        val field: String,
        val rejectedValue: Any?,
        val reason: String?,
    ) {
        companion object {
            private const val FIELD_POSITION = 1

            @JvmStatic
            fun from(violations: Set<ConstraintViolation<*>>): List<ConstraintViolationError> =
                violations.map { v ->
                    ConstraintViolationError(
                        field = v.propertyPath.toString().split(".")[FIELD_POSITION],
                        rejectedValue = v.invalidValue?.toString(),
                        reason = v.message,
                    )
                }
        }
    }
}
```

**Step 2: GlobalExceptionHandler.kt 작성**

```kotlin
package com.dh.ondot.core.exception

import com.dh.ondot.core.ErrorResponse
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.ConstraintViolationException
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBindingException(e: BindException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.FIELD_ERROR, e.bindingResult)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.URL_PARAMETER_ERROR, e.constraintViolations)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingParam(e: MissingServletRequestParameterException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.URL_PARAMETER_ERROR)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleClientAbortException(e: ClientAbortException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.ALREADY_DISCONNECTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.INVALID_JSON)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequestException(e: BadRequestException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(e: UnauthorizedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbiddenException(e: ForbiddenException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.NO_RESOURCE_FOUND)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.METHOD_NOT_SUPPORTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflictException(e: ConflictException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun handleTooManyRequestsException(e: TooManyRequestsException): ErrorResponse {
        log.warn(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalServerErrorException(e: InternalServerException): ErrorResponse {
        log.error(e.message, e)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorResponse {
        log.error(e.message, e)
        return ErrorResponse(ErrorCode.SERVER_ERROR)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleBadGatewayException(e: BadGatewayException): ErrorResponse {
        log.error(e.message)
        return ErrorResponse(e)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleServiceUnavailableException(e: ServiceUnavailableException): ErrorResponse {
        log.error(e.message)
        return ErrorResponse(e)
    }
}
```

**Step 3: Java 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/ErrorResponse.java
rm src/main/java/com/dh/ondot/core/exception/GlobalExceptionHandler.java
```

**Step 4: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 5: 커밋**

```bash
git add -A
git commit -m "refactor: ErrorResponse, GlobalExceptionHandler를 Kotlin으로 전환"
```

---

## Task 8: core config 클래스 전환 (12개)

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/config/` (12개 Java 파일)
- Create: `src/main/kotlin/com/dh/ondot/core/config/` (Kotlin 파일)

**Step 1: AppConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableAsync
@EnableRetry
@EnableJpaAuditing
@EnableScheduling
class AppConfig
```

**Step 2: AsyncConstants.kt**

```kotlin
package com.dh.ondot.core.config

object AsyncConstants {
    const val EVENT_ASYNC_TASK_EXECUTOR = "eventAsyncExecutor"
    const val DISCORD_ASYNC_TASK_EXECUTOR = "discordAsyncExecutor"
}
```

**주의 (Java interop):** Java 코드에서 `import static com.dh.ondot.core.config.AsyncConstants.*;` 패턴으로 사용중. Kotlin `object` + `const val`은 Java에서 `AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR` 로 접근 가능. `@JvmField`는 `const val`에 불필요 (이미 컴파일 타임 상수).

**Step 3: AsyncProperties.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "async")
data class AsyncProperties(
    val event: EventConfig = EventConfig(),
    val discord: DiscordConfig = DiscordConfig(),
) {
    data class EventConfig(
        val corePoolSize: Int = 4,
        val maxPoolSize: Int = 8,
        val queueCapacity: Int = 500,
    )

    data class DiscordConfig(
        val corePoolSize: Int = 2,
        val maxPoolSize: Int = 4,
        val queueCapacity: Int = 100,
    )
}
```

**Step 4: AsyncConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableConfigurationProperties(AsyncProperties::class)
class AsyncConfig(
    private val asyncProperties: AsyncProperties,
) : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(name = [AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR])
    fun eventAsyncExecutor(): Executor {
        val config = asyncProperties.event
        return createThreadPoolTaskExecutor(
            config.corePoolSize, config.maxPoolSize, config.queueCapacity, "event-"
        )
    }

    @Bean(name = [AsyncConstants.DISCORD_ASYNC_TASK_EXECUTOR])
    fun discordAsyncExecutor(): Executor {
        val config = asyncProperties.discord
        return createThreadPoolTaskExecutor(
            config.corePoolSize, config.maxPoolSize, config.queueCapacity, "discord-"
        )
    }

    private fun createThreadPoolTaskExecutor(
        corePoolSize: Int, maxPoolSize: Int, queueCapacity: Int, threadNamePrefix: String,
    ): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = corePoolSize
        executor.maxPoolSize = maxPoolSize
        executor.queueCapacity = queueCapacity
        executor.setThreadNamePrefix(threadNamePrefix)
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(10)
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex, method, params ->
            log.error(
                "Async execution failed in {}.{} with params: {}",
                method.declaringClass.simpleName,
                method.name,
                params.contentToString(), ex
            )
        }
    }
}
```

**Step 5: QueryDslConfig.kt**

```kotlin
package com.dh.ondot.core.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryDslConfig(
    @PersistenceContext
    private val entityManager: EntityManager,
) {

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory = JPAQueryFactory(entityManager)
}
```

**Step 6: WebConfig.kt**

```kotlin
package com.dh.ondot.core.config

import com.dh.ondot.core.TokenInterceptor
import com.dh.ondot.member.core.OauthProviderConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val oauthProviderConverter: OauthProviderConverter,
    private val tokenInterceptor: TokenInterceptor,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(oauthProviderConverter)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tokenInterceptor)
            .addPathPatterns("/members/**", "/alarms/**", "/places/**", "/schedules/**")
            .excludePathPatterns(
                "/schedules/*/issues",
                "/schedules/*/preparation"
            )
    }
}
```

**Step 7: OpenApiConfig.kt**

```kotlin
package com.dh.ondot.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class OpenApiConfig(
    private val environment: Environment,
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "AccessToken"
        val activeProfile = environment.getProperty("spring.profiles.active", "local")
        val serverUrl = System.getenv("PROD_SERVER_URL")?.takeIf { it.isNotBlank() }
            ?: "http://localhost:8080"

        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .servers(listOf(Server().url(serverUrl).description("$activeProfile server")))
            .info(
                Info().title("On-Dot API Documentation")
                    .description("Team DH's On-Dot service API specification.")
                    .version("1.0")
            )
    }
}
```

**Step 8: P6SpyFormatter.kt**

```kotlin
package com.dh.ondot.core.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.hibernate.engine.jdbc.internal.FormatStyle
import org.springframework.context.annotation.Configuration
import java.util.Locale

@Configuration
class P6SpyFormatter : MessageFormattingStrategy {

    @PostConstruct
    fun setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().logMessageFormat = this::class.java.name
    }

    override fun formatMessage(
        connectionId: Int, now: String?, elapsed: Long,
        category: String?, prepared: String?, sql: String?, url: String?,
    ): String {
        val formattedSql = formatSql(category, sql)
        return "[$category] | $elapsed ms | $formattedSql"
    }

    private fun formatSql(category: String?, sql: String?): String {
        if (!sql.isNullOrBlank() && category == Category.STATEMENT.name) {
            val trimmedSQL = sql.trim().lowercase(Locale.ROOT)
            val formatter = when {
                trimmedSQL.startsWith("create") ||
                    trimmedSQL.startsWith("alter") ||
                    trimmedSQL.startsWith("comment") -> FormatStyle.DDL.formatter

                else -> FormatStyle.BASIC.formatter
            }
            return formatter.format(sql)
        }
        return sql ?: ""
    }
}
```

**Step 9: SpringAiConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringAiConfig {

    @Bean
    fun chatClient(
        @Value("\${spring.ai.openai.api-key}") apiKey: String,
        @Value("\${spring.ai.openai.model}") model: String,
    ): ChatClient {
        val openAiApi = OpenAiApi.builder()
            .apiKey(apiKey)
            .build()

        val chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model(model)
                    .build()
            )
            .build()

        return ChatClient.create(chatModel)
    }
}
```

**Step 10: OdsayApiConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "odsay")
data class OdsayApiConfig(
    val baseUrl: String,
    val apiKey: String,
)
```

**Step 11: OdsayRestClientConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class OdsayRestClientConfig {

    @Bean
    @Qualifier("odsayRestClient")
    fun odsayRestClient(props: OdsayApiConfig): RestClient =
        RestClient.builder()
            .baseUrl(props.baseUrl)
            .build()
}
```

**Step 12: RestClientConfig.kt**

```kotlin
package com.dh.ondot.core.config

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restClient(): RestClient {
        val settings = ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(Duration.ofSeconds(3))
            .withReadTimeout(Duration.ofSeconds(1))

        val factory = ClientHttpRequestFactoryBuilder
            .detect()
            .build(settings)

        return RestClient.builder()
            .requestFactory(factory)
            .build()
    }
}
```

**Step 13: Java config 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/config/AppConfig.java
rm src/main/java/com/dh/ondot/core/config/AsyncConfig.java
rm src/main/java/com/dh/ondot/core/config/AsyncConstants.java
rm src/main/java/com/dh/ondot/core/config/AsyncProperties.java
rm src/main/java/com/dh/ondot/core/config/QueryDslConfig.java
rm src/main/java/com/dh/ondot/core/config/WebConfig.java
rm src/main/java/com/dh/ondot/core/config/OpenApiConfig.java
rm src/main/java/com/dh/ondot/core/config/P6SpyFormatter.java
rm src/main/java/com/dh/ondot/core/config/SpringAiConfig.java
rm src/main/java/com/dh/ondot/core/config/OdsayApiConfig.java
rm src/main/java/com/dh/ondot/core/config/OdsayRestClientConfig.java
rm src/main/java/com/dh/ondot/core/config/RestClientConfig.java
```

**Step 14: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 15: 커밋**

```bash
git add -A
git commit -m "refactor: core config 클래스 12개를 Kotlin으로 전환"
```

---

## Task 9: core util 클래스 전환 (TimeUtils, GeoUtils)

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/util/TimeUtils.java`
- Delete: `src/main/java/com/dh/ondot/core/util/GeoUtils.java`
- Create: `src/main/kotlin/com/dh/ondot/core/util/TimeUtils.kt`
- Create: `src/main/kotlin/com/dh/ondot/core/util/GeoUtils.kt`

**Step 1: TimeUtils.kt 작성**

baro 패턴(`Time.kt`)을 참고하되, 기존 함수 시그니처를 유지 (Java interop):

```kotlin
package com.dh.ondot.core.util

import java.time.*

object TimeUtils {
    private val DEFAULT_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    @JvmStatic
    fun toSeoulDateTime(instant: Instant?): LocalDateTime? {
        return instant?.atZone(DEFAULT_ZONE)?.toLocalDateTime()
    }

    @JvmStatic
    fun toInstant(localDateTime: LocalDateTime): Instant {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant()
    }

    @JvmStatic
    fun nowSeoulDateTime(): LocalDateTime {
        return LocalDateTime.now(DEFAULT_ZONE)
    }

    @JvmStatic
    fun nowSeoulDate(): LocalDate {
        return LocalDate.now(DEFAULT_ZONE)
    }

    @JvmStatic
    fun nowSeoulInstant(): Instant {
        return ZonedDateTime.now(DEFAULT_ZONE).toInstant()
    }

    @JvmStatic
    fun toSeoulTime(instant: Instant?): LocalTime? {
        return instant?.atZone(DEFAULT_ZONE)?.toLocalTime()
    }

    @JvmStatic
    fun findEarliestAfterNow(time1: Instant?, time2: Instant?): Instant? {
        val now = Instant.now()
        val isTime1Valid = time1 != null && time1.isAfter(now)
        val isTime2Valid = time2 != null && time2.isAfter(now)

        return when {
            !isTime1Valid && !isTime2Valid -> null
            !isTime1Valid -> time2
            !isTime2Valid -> time1
            else -> if (time1!!.isBefore(time2!!)) time1 else time2
        }
    }
}
```

**주의 (Java interop):** Lombok `@UtilityClass`는 `TimeUtils.toSeoulDateTime(...)` 처럼 static 메서드 호출을 생성한다. Kotlin `object` + `@JvmStatic`은 Java에서 동일하게 `TimeUtils.toSeoulDateTime(...)` 으로 호출 가능.

**Step 2: GeoUtils.kt 작성**

```kotlin
package com.dh.ondot.core.util

import kotlin.math.*

object GeoUtils {

    @JvmStatic
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // meters
    }
}
```

**Step 3: Java 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/util/TimeUtils.java
rm src/main/java/com/dh/ondot/core/util/GeoUtils.java
```

**Step 4: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL — 기존 TimeUtilsTest.java 가 Kotlin TimeUtils 를 참조하여 통과

**Step 5: 커밋**

```bash
git add -A
git commit -m "refactor: TimeUtils, GeoUtils를 Kotlin으로 전환"
```

---

## Task 10: TokenInterceptor 전환

**Files:**
- Delete: `src/main/java/com/dh/ondot/core/TokenInterceptor.java`
- Create: `src/main/kotlin/com/dh/ondot/core/TokenInterceptor.kt`

**Step 1: TokenInterceptor.kt 작성**

```kotlin
package com.dh.ondot.core

import com.dh.ondot.member.application.TokenFacade
import com.dh.ondot.member.core.exception.InvalidTokenHeaderException
import com.dh.ondot.member.core.exception.TokenMissingException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class TokenInterceptor(
    private val tokenFacade: TokenFacade,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val jwtToken = request.getHeader(AUTHORIZATION_HEADER)
                ?.takeIf { it.isNotBlank() }
                ?: throw TokenMissingException()

            val accessToken = if (jwtToken.startsWith(BEARER_PREFIX)) {
                jwtToken.substring(BEARER_PREFIX.length)
            } else {
                throw InvalidTokenHeaderException()
            }

            val memberId = tokenFacade.validateToken(accessToken)
            request.setAttribute("memberId", memberId)
        }
        return true
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
```

**Step 2: Java 파일 삭제**

```bash
rm src/main/java/com/dh/ondot/core/TokenInterceptor.java
```

**Step 3: 빌드 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 4: 커밋**

```bash
git add -A
git commit -m "refactor: TokenInterceptor를 Kotlin으로 전환"
```

---

## Task 11: Java core 디렉토리 정리 + Lombok 임시 의존성 정리

**Step 1: 빈 Java core 디렉토리 확인 및 삭제**

core 디렉토리 하위에 남은 Java 파일이 없는지 확인:

```bash
find src/main/java/com/dh/ondot/core -name "*.java" -type f
```

Expected: 출력 없음 (모든 파일이 Kotlin으로 전환됨)

빈 디렉토리 삭제:

```bash
rm -rf src/main/java/com/dh/ondot/core
```

**Step 2: Lombok 임시 의존성 유지 확인**

gradle/core.gradle에 추가한 Lombok 의존성은 **member, schedule, notification 모듈이 여전히 Lombok을 사용하므로 유지**해야 한다. TODO 주석이 있는지 확인.

**Step 3: 전체 빌드 + 테스트 최종 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL — 모든 테스트 통과

**Step 4: 커밋**

```bash
git add -A
git commit -m "refactor: Java core 디렉토리 정리 완료"
```

---

## Task 12: TimeUtilsTest를 Kotlin으로 전환

**Files:**
- Delete: `src/test/java/com/dh/ondot/core/util/TimeUtilsTest.java`
- Create: `src/test/kotlin/com/dh/ondot/core/util/TimeUtilsTest.kt`

**Step 1: TimeUtilsTest.kt 작성**

```kotlin
package com.dh.ondot.core.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@DisplayName("DateTimeUtils 테스트")
class TimeUtilsTest {

    @Test
    @DisplayName("Instant를 서울 LocalDateTime으로 변환한다")
    fun toSeoulDateTime_ConvertsInstantToLocalDateTime() {
        val instant = Instant.now()

        val result = TimeUtils.toSeoulDateTime(instant)

        assertThat(result).isNotNull
        val expected = instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("null Instant를 전달하면 null을 반환한다")
    fun toSeoulDateTime_NullInstant_ReturnsNull() {
        val result = TimeUtils.toSeoulDateTime(null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("LocalDateTime을 서울 시간 기준 Instant로 변환한다")
    fun toInstant_ConvertsLocalDateTimeToInstant() {
        val localDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

        val result = TimeUtils.toInstant(localDateTime)

        assertThat(result).isNotNull
        val expected = localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("Instant를 서울 시간 LocalTime으로 변환한다")
    fun toSeoulTime_ConvertsInstantToLocalTime() {
        val testDateTime = LocalDateTime.of(2024, 1, 1, 15, 30, 45)
        val instant = TimeUtils.toInstant(testDateTime)

        val result = TimeUtils.toSeoulTime(instant)

        assertThat(result).isEqualTo(LocalTime.of(15, 30, 45))
    }

    @Test
    @DisplayName("null Instant를 전달하면 null LocalTime을 반환한다")
    fun toSeoulTime_NullInstant_ReturnsNull() {
        val result = TimeUtils.toSeoulTime(null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("현재 서울 시간을 LocalDateTime으로 반환한다")
    fun nowSeoulDateTime_ReturnsCurrentSeoulTime() {
        val result = TimeUtils.nowSeoulDateTime()

        assertThat(result).isNotNull
        assertThat(result).isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(1))
        assertThat(result).isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(1))
    }

    @Test
    @DisplayName("두 Instant 중 현재 시간 이후의 가장 빠른 시간을 찾는다")
    fun findEarliestAfterNow_BothValid_ReturnsEarlier() {
        val now = Instant.now()
        val earlier = now.plusSeconds(3600)
        val later = now.plusSeconds(7200)

        val result = TimeUtils.findEarliestAfterNow(later, earlier)

        assertThat(result).isEqualTo(earlier)
    }

    @Test
    @DisplayName("하나만 유효한 시간인 경우 유효한 시간을 반환한다")
    fun findEarliestAfterNow_OneValid_ReturnsValid() {
        val now = Instant.now()
        val future = now.plusSeconds(3600)
        val past = now.minusSeconds(3600)

        val result = TimeUtils.findEarliestAfterNow(past, future)

        assertThat(result).isEqualTo(future)
    }

    @Test
    @DisplayName("둘 다 과거 시간이면 null을 반환한다")
    fun findEarliestAfterNow_BothPast_ReturnsNull() {
        val now = Instant.now()
        val past1 = now.minusSeconds(3600)
        val past2 = now.minusSeconds(1800)

        val result = TimeUtils.findEarliestAfterNow(past1, past2)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("둘 다 null이면 null을 반환한다")
    fun findEarliestAfterNow_BothNull_ReturnsNull() {
        val result = TimeUtils.findEarliestAfterNow(null, null)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("하나가 null이고 다른 하나가 유효하면 유효한 시간을 반환한다")
    fun findEarliestAfterNow_OneNullOneValid_ReturnsValid() {
        val future = Instant.now().plusSeconds(3600)

        val result1 = TimeUtils.findEarliestAfterNow(null, future)
        val result2 = TimeUtils.findEarliestAfterNow(future, null)

        assertThat(result1).isEqualTo(future)
        assertThat(result2).isEqualTo(future)
    }

    @Test
    @DisplayName("두 시간이 동일한 경우 해당 시간을 반환한다")
    fun findEarliestAfterNow_SameTime_ReturnsSameTime() {
        val future = Instant.now().plusSeconds(3600)

        val result = TimeUtils.findEarliestAfterNow(future, future)

        assertThat(result).isEqualTo(future)
    }
}
```

**Step 2: Java 파일 삭제**

```bash
rm src/test/java/com/dh/ondot/core/util/TimeUtilsTest.java
```

**Step 3: 테스트 실행**

```bash
./gradlew test --tests "com.dh.ondot.core.util.TimeUtilsTest"
```

Expected: All 11 tests PASSED

**Step 4: 전체 빌드 최종 검증**

```bash
./gradlew clean build
```

Expected: BUILD SUCCESSFUL

**Step 5: 커밋**

```bash
git add -A
git commit -m "refactor: TimeUtilsTest를 Kotlin으로 전환"
```

---

## Summary

| Task | 내용 | 파일 수 |
|------|------|---------|
| 1 | gradle.properties 생성 | 1 create |
| 2 | gradle 서브파일 분리 | 6 create |
| 3 | build.gradle 전환 | 1 modify |
| 4 | Kotlin 소스 디렉토리 생성 | dirs only |
| 5 | AggregateRoot + BaseTimeEntity | 2 delete, 2 create |
| 6 | Exception 클래스 (11개) | 11 delete, 3 create |
| 7 | ErrorResponse + GlobalExceptionHandler | 2 delete, 2 create |
| 8 | Config 클래스 (12개) | 12 delete, 12 create |
| 9 | Util 클래스 (2개) | 2 delete, 2 create |
| 10 | TokenInterceptor | 1 delete, 1 create |
| 11 | Java core 디렉토리 정리 | cleanup |
| 12 | TimeUtilsTest 전환 | 1 delete, 1 create |
| **Total** | | **31 Java 삭제, 24 Kotlin 생성** |
