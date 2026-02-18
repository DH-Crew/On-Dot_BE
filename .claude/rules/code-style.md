# Code Style Rules

## Logging
- `log.info()` 사용 금지. warn/error만 사용한다.
- 정상 흐름은 로그 없이 처리하고, 비정상 상황만 warn/error로 남긴다.

## Import
- FQCN(Fully Qualified Class Name)을 코드 본문에 직접 사용하지 않는다.
- 반드시 `import` 문으로 올려서 사용한다.
- Bad: `java.io.FileInputStream(path)`
- Good: `import java.io.FileInputStream` + `FileInputStream(path)`
