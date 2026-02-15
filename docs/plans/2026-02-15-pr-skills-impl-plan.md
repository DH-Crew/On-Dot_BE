# PR Skills Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create two personal Claude skills (`feature-pr`, `release-pr`) for automating PR workflows in the Santiago project.

**Architecture:** Two independent SKILL.md files in `~/.claude/skills/`, each containing flowcharts, step-by-step instructions, and exact commands for Claude to follow.

**Tech Stack:** Claude skills (Markdown + YAML frontmatter), `gh` CLI, `git`

---

### Task 1: Create skills directory structure

**Files:**
- Create: `~/.claude/skills/feature-pr/SKILL.md` (placeholder)
- Create: `~/.claude/skills/release-pr/SKILL.md` (placeholder)

**Step 1: Create directories**

```bash
mkdir -p ~/.claude/skills/feature-pr
mkdir -p ~/.claude/skills/release-pr
```

**Step 2: Verify**

```bash
ls -la ~/.claude/skills/
```

Expected: `feature-pr/` and `release-pr/` directories exist.

---

### Task 2: Write `feature-pr` SKILL.md

**Files:**
- Create: `~/.claude/skills/feature-pr/SKILL.md`

**Step 1: Write the skill file**

The skill must include:

1. **Frontmatter**: `name: feature-pr`, `description: Use when starting or finishing feature work...`
2. **Overview**: Lifecycle skill with start/finish phases
3. **Flowchart**: Decision graph for args routing (`start` vs `finish`)
4. **Start Phase** instructions:
   - Verify on develop, pull latest
   - Ask user for type (feat/fix/refactor) + Linear ID via multiple choice
   - Create and switch to branch `{type}/DH-{id}`
5. **Finish Phase** instructions:
   - Verify NOT on develop/main
   - Run tests (project uses `./gradlew test`)
   - Review changes with `git diff` and `git status`
   - Create meaningful commits (conventional commits, Korean)
   - Push to remote
   - Create squash PR with `gh pr create`:
     - Title from branch context
     - Body follows PR template exactly:
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

       ## ✅ Check List
       - [x] Have all tests passed?
       - [x] Have all commits been pushed?
       - [x] Did you verify the target branch for the merge?
       - [ ] Did you assign the appropriate assignee(s)?
       - [ ] Did you set the correct label(s)?

       ## 📸 Test Screenshot

       ## Additional Description
       ```
     - Label: auto-detect (`feat` -> `🚀 FEAT`, `fix` -> `🚨 FIX`, `refactor` -> `🔋 REFACTOR`)
     - Assignee: auto-assign
6. **Red Flags**: Never create PR with failing tests, never PR to wrong branch

**Step 2: Verify skill is discoverable**

```bash
cat ~/.claude/skills/feature-pr/SKILL.md | head -5
```

Expected: Valid YAML frontmatter with name and description.

**Step 3: Commit**

```bash
git add -f ~/.claude/skills/feature-pr/SKILL.md  # won't work - not in repo
```

Note: Skills live in `~/.claude/skills/`, outside the repo. No git commit needed for skill files themselves.

---

### Task 3: Write `release-pr` SKILL.md

**Files:**
- Create: `~/.claude/skills/release-pr/SKILL.md`

**Step 1: Write the skill file**

The skill must include:

1. **Frontmatter**: `name: release-pr`, `description: Use when releasing accumulated work from develop to main...`
2. **Overview**: Manual release workflow with version confirmation
3. **Flowchart**: Linear flow with two user confirmation gates
4. **The Process**:
   - Verify on develop branch, pull latest
   - Gather commits since last merge to main: `git log main..develop --oneline`
   - For each commit, determine:
     - Change level (Major/Minor/Patch) based on scope of changes
     - Author GitHub ID from `git log --format='%an'`
   - Create PR with `gh pr create`:
     - Title: `Release: (version TBD)`
     - Body:
       ```
       ## Summary

       ### Major Changes
       - {description} @{author-github-id}

       ### Minor Changes
       - {description} @{author-github-id}

       ### Patch Changes
       - {description} @{author-github-id}
       ```
     - Categorization rules:
       - **Major**: Breaking changes, incompatible API changes
       - **Minor**: New features (`feat:` commits), new functionality
       - **Patch**: Bug fixes (`fix:`), tests (`test:`), docs (`docs:`), refactoring (`refactor:`), config (`chore:`)
   - **Gate 1**: Ask user "PR 내용 확인해주세요. 문제 없나요?"
   - **Gate 2**: Get current version from latest git tag, suggest next version based on highest change level, present as multiple choice
   - On confirm: Update PR title to `Release: {version}`, merge PR with `gh pr merge --merge`
5. **Red Flags**: Never merge without both confirmations, never skip version confirmation

**Step 2: Verify skill is discoverable**

```bash
cat ~/.claude/skills/release-pr/SKILL.md | head -5
```

Expected: Valid YAML frontmatter with name and description.

---

### Task 4: Manual smoke test

**Step 1: Verify both skills appear in skill list**

Start a new Claude Code session and check if `/feature-pr` and `/release-pr` are recognized.

**Step 2: Test feature-pr start (dry run)**

Invoke `/feature-pr start` and verify it:
- Checks current branch
- Asks for type + Linear ID
- Would create correct branch name

**Step 3: Test release-pr (dry run)**

Invoke `/release-pr` and verify it:
- Gathers commits correctly
- Categorizes changes
- Presents PR content for review

---

### Task 5: Commit design and plan docs

**Step 1: Commit**

```bash
git add docs/plans/2026-02-15-pr-skills-impl-plan.md
git commit -m "docs: PR 스킬 구현 계획 문서 작성"
```
