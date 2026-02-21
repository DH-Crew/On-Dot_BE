# Project Instructions

## 필수 워크플로우

### 작업 시작 플로우
1. 새 작업 요청을 받으면 `git status`로 워킹 트리 상태 확인
2. uncommitted changes가 없으면 (clean) → `/feature-pr start` 실행하여 브랜치 생성
3. uncommitted changes가 있으면 → 사용자에게 알리고 처리 방법 확인
4. 브랜치 생성 완료 후 → 실제 구현 작업 시작
- 브랜치가 이미 생성된 상태라면 start는 생략 가능하나, 브랜치 네이밍이 컨벤션(`feat/DH-{id}`, `fix/DH-{id}`, `refactor/DH-{id}`)을 따르는지 확인한다

### 작업 완료 플로우
- 작업이 완료되면: 반드시 `/feature-pr finish`를 실행한다
  - 직접 커밋/push/PR 생성을 하지 않는다

### 릴리스 플로우
- develop의 누적 작업을 main에 릴리스할 때: 반드시 `/release-pr`를 실행한다

## Auto Commit & Push

승인된 작업이 완료되면:
1. 즉시 커밋을 생성한다 (Co-Authored-By 헤더를 포함하지 않는다)
2. 현재 브랜치에 열려있는 PR이 있는 경우, push까지 진행한다
3. push 후 PR이 존재하면, 추가된 커밋 내용을 반영하여 PR title과 body를 업데이트한다

## Skills

### `/feature-pr` — Feature 브랜치 라이프사이클
- **start**: develop에서 새 feature 브랜치 생성 (`feat/DH-{id}`, `fix/DH-{id}`, `refactor/DH-{id}`)
  - 사용 시점: 설계/계획 문서 작성 등 파일을 생성하기 **전에** 실행하여 브랜치를 먼저 생성한다
- **finish**: 테스트 실행 → 커밋 → push → develop 대상 squash PR 생성
  - 사용 시점: feature 작업 완료 시

### `/release-pr` — Release PR 생성
- develop → main 릴리스 PR 생성
- 커밋 분류 (Major/Minor/Patch) 및 SemVer 자동 제안
- PR 내용 확인 → 버전 확인 → Discord 메시지 확인 3단계 게이트
- 릴리스 완료 후 Discord `#api-update` 채널에 변경사항 자동 전송 (webhook URL은 `~/.claude/env`에서 로드)
- 사용 시점: develop의 누적 작업을 main에 릴리스할 때

## Code Style

- 코드 리뷰에서 반복 발견되는 패턴은 `.claude/rules/code-style.md`에 규칙으로 추가한다.

## Skill 변경 관리

`.claude/skills/` 하위 스킬이 추가, 수정, 삭제되면 반드시 이 CLAUDE.md의 Skills 섹션도 함께 업데이트한다.
