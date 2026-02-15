# Project Instructions

## 필수 워크플로우

### 브랜치 작업 규칙
- feature/fix/refactor 작업을 시작할 때: 반드시 `/feature-pr start`를 먼저 실행한다
  - 브랜치가 이미 생성된 상태라면 start는 생략 가능하나, 브랜치 네이밍이 컨벤션(`feat/DH-{id}`, `fix/DH-{id}`, `refactor/DH-{id}`)을 따르는지 확인한다
- 작업이 완료되면: 반드시 `/feature-pr finish`를 실행한다
  - 직접 커밋/push/PR 생성을 하지 않는다
- develop의 누적 작업을 main에 릴리스할 때: 반드시 `/release-pr`를 실행한다

## Auto Commit & Push

승인된 작업이 완료되면:
1. 즉시 커밋을 생성한다
2. 현재 브랜치에 열려있는 PR이 있는 경우, push까지 진행한다
3. push 후 PR이 존재하면, 추가된 커밋 내용을 반영하여 PR title과 body를 업데이트한다

## Skills

### `/feature-pr` — Feature 브랜치 라이프사이클
- **start**: develop에서 새 feature 브랜치 생성 (`feat/DH-{id}`, `fix/DH-{id}`, `refactor/DH-{id}`)
- **finish**: 테스트 실행 → 커밋 → push → develop 대상 squash PR 생성
- 사용 시점: feature 작업 시작/완료 시

### `/release-pr` — Release PR 생성
- develop → main 릴리스 PR 생성
- 커밋 분류 (Major/Minor/Patch) 및 SemVer 자동 제안
- PR 내용 확인 → 버전 확인 2단계 게이트
- 사용 시점: develop의 누적 작업을 main에 릴리스할 때

## Skill 변경 관리

`.claude/skills/` 하위 스킬이 추가, 수정, 삭제되면 반드시 이 CLAUDE.md의 Skills 섹션도 함께 업데이트한다.
