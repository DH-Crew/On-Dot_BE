# Code Style Rules

## Logging
- `log.info()` 사용 금지. warn/error만 사용한다.
- 정상 흐름은 로그 없이 처리하고, 비정상 상황만 warn/error로 남긴다.

## Import
- FQCN(Fully Qualified Class Name)을 코드 본문에 직접 사용하지 않는다.
- 반드시 `import` 문으로 올려서 사용한다.
- Bad: `java.io.FileInputStream(path)`
- Good: `import java.io.FileInputStream` + `FileInputStream(path)`

## Annotation
- `@JvmStatic` 사용 금지. 프로젝트가 순수 Kotlin이므로 불필요하다.
  - 예외: `object` 선언의 메서드가 테스트에서 `mockStatic`으로 모킹되는 경우 `@JvmStatic` 필요.

## Null Safety
- `!!` (force unwrap) 사용 금지. `?.`, `?:`, `let` 등 null-safe 연산자를 사용한다.
- 외부 API 응답 등 nullable 값은 명시적 에러 처리(`?: throw`)로 처리한다.

## Exception
- 예외를 catch하여 새 예외로 래핑할 때, 원본 예외 메시지를 포함한다.
- `IllegalStateException`, `IllegalArgumentException` 등 generic 예외 대신 도메인 예외를 사용한다.
- catch 블록에서 예외를 삼키지(swallow) 않는다. 의도적 무시라면 `log.warn()`으로 남긴다.

## External API
- 외부 API RestClient에는 반드시 connect/read 타임아웃을 설정한다.
- 외부 API 호출은 domain 레이어가 아닌 infra/application 레이어에서 수행한다.

## JPA
- 새 NOT NULL 컬럼 추가 시 `columnDefinition`에 DEFAULT 값을 명시한다.
