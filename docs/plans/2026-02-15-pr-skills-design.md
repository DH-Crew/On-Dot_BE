# PR Skills Design

Two custom skills for automating PR workflows in the Santiago (On-Dot) project.

## Context

- **Git Flow**: feature branches -> develop -> main
- **Branch naming**: `{type}/DH-{linear-id}` (e.g., `feat/DH-6`, `fix/DH-12`)
- **Commit convention**: Korean conventional commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`)
- **PR template**: Issue Number, As-Is, To-Be, Checklist, Test Screenshot, Additional Description

---

## Skill 1: `feature-pr`

Lifecycle skill with two phases, invoked via args.

### Start Phase (`/feature-pr start`)

1. Verify on develop branch (switch if not)
2. Pull latest develop
3. Ask user: type (`feat`/`fix`/`refactor`) + Linear ID (e.g., `DH-6`)
4. Create branch: `{type}/DH-{id}`
5. Switch to new branch

### Finish Phase (`/feature-pr finish`)

1. Verify NOT on develop/main (abort if so)
2. Run tests — must pass before proceeding
3. Review all changes (staged + unstaged + untracked)
4. Create meaningful commits in appropriate units (conventional commits, Korean)
5. Push branch to remote
6. Create squash PR to develop:
   - **Title**: Inferred from branch prefix + Linear ID + brief description
   - **Body** (follows PR template):
     ```
     ## Issue Number
     DH-{id}

     ## As-Is
     ### 1. 기존 동작 및 문제 상황
     - {기존에 어떤 동작/상태였는지}
     - {어떤 문제가 발생했는지}

     ### 2. 대안 분석 (각 접근 방식의 장단점)
     - {방식 A}: {장점} / {단점}
     - {방식 B}: {장점} / {단점}

     ## To-Be
     ### 1. 최종 구현 결과
     - {구현된 핵심 내용}

     ### 2. 기존 대비 변경 내역
     - {변경 전} → {변경 후}

     ### 3. 미해결 이슈 및 향후 검토 사항
     - {남은 이슈 또는 추가 검토 필요 사항}

     ## Check List
     - [x] Have all tests passed?
     - [x] Have all commits been pushed?
     - [x] Did you verify the target branch for the merge?
     - [ ] Did you assign the appropriate assignee(s)?
     - [ ] Did you set the correct label(s)?

     ## Test Screenshot

     ## Additional Description
     ```
   - **Label**: Auto-detect from branch prefix (`feat` -> FEAT, `fix` -> FIX, `refactor` -> REFACTOR)
   - **Assignee**: Auto-assign current user

### Trigger

- **Start**: Beginning of feature work, after design/planning is done
- **Finish**: After all implementation, code review, and tests are complete

---

## Skill 2: `release-pr`

Standalone skill for creating release PRs from develop to main. Manually invoked by user.

### Flow

1. Verify on develop branch
2. Pull latest develop & main
3. Gather commits since last merge to main
4. Analyze each commit's change scope and author
5. Create PR (develop -> main):
   - **Title**: `Release: (version TBD)`
   - **Body**:
     ```
     ## Summary

     ### Major Changes
     - {description} @{author-github-id}

     ### Minor Changes
     - {description} @{author-github-id}

     ### Patch Changes
     - {description} @{author-github-id}
     ```
   - Categorization:
     - **Major**: Breaking changes, incompatible API changes
     - **Minor**: New features, new functionality (backward compatible)
     - **Patch**: Bug fixes, test additions, docs, refactoring, config changes

6. Ask user: "PR 내용 확인해주세요. 문제 없나요?"
7. User confirms -> Suggest version based on highest change level:
   - Major changes present -> major bump (e.g., 1.0.1 -> 2.0.0)
   - Minor changes highest -> minor bump (e.g., 1.0.1 -> 1.1.0)
   - Patch only -> patch bump (e.g., 1.0.1 -> 1.0.2)
   - Present as multiple choice for user to confirm/override
8. User confirms version -> Update PR title to `Release: {version}` -> Merge PR

### Trigger

- Manually invoked when user decides to release accumulated work from develop to main

---

## Skill Location

Project skills directory: `.claude/skills/`

```
.claude/skills/
  feature-pr/
    SKILL.md
  release-pr/
    SKILL.md
```

## Integration

- `feature-pr start` is invoked after brainstorming/planning
- Other skills (implementation, code-review, test) run in between
- `feature-pr finish` is invoked when all work is complete
- `release-pr` is invoked independently when user decides to release
