# Project Instructions

## Auto Commit & Push

승인된 작업이 완료되면:
1. 즉시 커밋을 생성한다
2. 현재 브랜치에 열려있는 PR이 있는 경우, push까지 진행한다

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
