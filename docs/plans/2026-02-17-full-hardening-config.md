# Full Hardening Config — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Apply full hardening (Android + Functions + CI + root tooling) per design `docs/plans/2026-02-17-full-hardening-config-design.md`: version alignment, Detekt, Node 24, stricter ESLint/TS, CI pins and dependency-review and CodeQL, Prettier ignore, verify script, doc updates.

**Architecture:** No behavior changes to app or functions logic. Config and tooling only. Single source of truth: `gradle/libs.versions.toml` (Android), `functions/package.json` + `functions/.nvmrc` (Node 24). CI and docs updated to match.

**Tech Stack:** Gradle, Detekt, Node 24, TypeScript, ESLint, Prettier, GitHub Actions (pinned SHAs), dependency-review, CodeQL.

---

## Task 1: Add Detekt and config

**Files:**

- Create: `config/detekt.yml`
- Modify: `gradle/libs.versions.toml` (add detekt version and plugin)
- Modify: `build.gradle.kts` (apply detekt plugin)
- Modify: `app/build.gradle.kts` and `feature-ai/build.gradle.kts` (apply detekt if needed per plugin docs)

**Step 1: Add Detekt version to catalog**

In `gradle/libs.versions.toml` under `[versions]` add:

```toml
detekt = "1.24.0"
```

Under `[plugins]` add:

```toml
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

**Step 2: Create config/detekt.yml**

Create `config/detekt.yml` with a conservative rule set (e.g. enable a few complexity/style rules; disable experimental). Example minimal:

```yaml
build:
  maxIssues: 0
  excludeCorrectable: false
config:
  validation: true
complexity:
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    threshold: 6
style:
  MagicNumber:
    active: false
```

**Step 3: Apply Detekt in root build.gradle.kts**

After existing plugins block, add:

```kotlin
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Configure detekt extension if needed (config path, baseline)
}
```

Or apply plugin via alias(libs.plugins.detekt) and configure detekt extension to use `config/detekt.yml` and `config/detekt-baseline.xml` (created in next step).

**Step 4: Generate baseline**

Run: `./gradlew detekt`
Expected: May report issues. Then run with baseline generation (e.g. `detektBaseline` if available, or add baseline path and re-run to create empty baseline, then add existing issues to baseline).
Command: `./gradlew detektBaseline` (creates `config/detekt-baseline.xml`). If no such task, run `./gradlew detekt` and fix or add excludes in config so build passes; commit a baseline file if the plugin creates one.

**Step 5: Commit**

```bash
git add config/detekt.yml config/detekt-baseline.xml gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts feature-ai/build.gradle.kts
git commit -m "chore(android): add Detekt and baseline"
```

---

## Task 2: Wire Detekt into CI and fix baseline

**Files:**

- Modify: `.github/workflows/android-ci.yml`

**Step 1: Add Detekt to Android CI job**

In the "Build, test, and lint" step, add Detekt: e.g. `./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest :app:lintDebug detekt --stacktrace --no-daemon` (or `:app:detekt :feature-ai:detekt` if per-module).

**Step 2: Run CI locally**

Run the same gradle command from repo root. Ensure it passes (baseline or config must allow current code).

**Step 3: Commit**

```bash
git add .github/workflows/android-ci.yml
git commit -m "ci(android): run Detekt in Android CI"
```

---

## Task 3: Align README and spec with libs.versions.toml

**Files:**

- Modify: `README.md`
- Modify: `specs/NOVACHAT_ARCHITECTURE_SPEC.md`

**Step 1: Update README tech stack**

Replace Kotlin 2.2.21 with the version from `gradle/libs.versions.toml` (e.g. `Kotlin 2.3.10`). Add a line: "Android versions: see `gradle/libs.versions.toml`."

**Step 2: Update spec §5**

In "Tech stack", add: "Android: versions in `gradle/libs.versions.toml`. Backend: Node 24 (see `functions/package.json` engines and `functions/.nvmrc`)."

**Step 3: Commit**

```bash
git add README.md specs/NOVACHAT_ARCHITECTURE_SPEC.md
git commit -m "docs: align README and spec with version sources"
```

---

## Task 4: Functions — Node 24, .nvmrc, README

**Files:**

- Modify: `functions/package.json` (engines)
- Create: `functions/.nvmrc`
- Modify: `functions/README.md`

**Step 1: Set engines in package.json**

In `functions/package.json`, set `"engines": { "node": "24" }`.

**Step 2: Create .nvmrc**

Create `functions/.nvmrc` with single line: `24`

**Step 3: Update functions/README.md**

Set "Node.js 24" in Runtime section. Add: "Version is defined by `engines` in package.json and `.nvmrc`."

**Step 4: Commit**

```bash
git add functions/package.json functions/.nvmrc functions/README.md
git commit -m "chore(functions): pin Node 24, add .nvmrc and update README"
```

---

## Task 5: Functions — TypeScript strictness

**Files:**

- Modify: `functions/tsconfig.json`

**Step 1: Enable strict**

Ensure `"strict": true` in compilerOptions. If adding `"noUncheckedIndexedAccess": true` causes many errors, leave it out and add a comment: "Consider noUncheckedIndexedAccess when codebase is updated."

**Step 2: Build**

Run: `cd functions && npm run build`
Expected: Success. If errors, fix minimal set or revert noUncheckedIndexedAccess.

**Step 3: Commit**

```bash
git add functions/tsconfig.json
git commit -m "chore(functions): ensure TypeScript strict mode"
```

---

## Task 6: Functions — ESLint hardening

**Files:**

- Modify: `functions/eslint.config.ts`

**Step 1: Add stricter rules**

Add a few rules (e.g. `@typescript-eslint/no-floating-promises`, `@typescript-eslint/no-misused-promises`) or extend with a strict preset. If `strictTypeChecked` causes too many failures, add only 1–2 rules and fix violations.

**Step 2: Run lint and fix**

Run: `cd functions && npm run lint`
Fix any new violations. Ensure: `npm run lint` passes.

**Step 3: Commit**

```bash
git add functions/eslint.config.ts functions/src/
git commit -m "chore(functions): tighten ESLint rules and fix violations"
```

---

## Task 7: Functions — npm audit script and CI

**Files:**

- Modify: `functions/package.json` (scripts)
- Modify: `.github/workflows/functions-ci.yml`

**Step 1: Add audit script**

In `functions/package.json` scripts, add: `"audit": "npm audit --audit-level=high"`

**Step 2: Use Node 24 in Functions CI**

In `.github/workflows/functions-ci.yml`, set Node to 24: `node-version: '24'` (or use `node-version-file: 'functions/.nvmrc'`).

**Step 3: Add audit step in Functions CI**

After "Test", add step: run `npm run audit` (or `npm audit --audit-level=high`). If it fails, fix or document allow-list.

**Step 4: Commit**

```bash
git add functions/package.json .github/workflows/functions-ci.yml
git commit -m "ci(functions): Node 24, add npm audit step"
```

---

## Task 8: Pin GitHub Actions to SHAs

**Files:**

- Modify: `.github/workflows/android-ci.yml`
- Modify: `.github/workflows/functions-ci.yml`
- Modify: `.github/workflows/security.yml` (if exists)
- Modify: `.github/workflows/release.yml` (if exists)

**Step 1: Look up current recommended SHAs**

For each action (e.g. actions/checkout@v6, actions/setup-java@v4, actions/setup-node@v4, actions/upload-artifact@v4, gradle/wrapper-validation-action@v2), use the SHA from the action’s README (e.g. https://github.com/actions/checkout — use the commit SHA for v6).

**Step 2: Replace tags with SHA**

Replace e.g. `actions/checkout@v6` with `actions/checkout@<sha>`. Do for all workflows.

**Step 3: Commit**

```bash
git add .github/workflows/
git commit -m "ci: pin actions to full SHA"
```

---

## Task 9: Add dependency-review workflow

**Files:**

- Create: `.github/workflows/dependency-review.yml` (or add job to existing PR workflow)

**Step 1: Add workflow**

Create workflow that runs on pull_request to main, uses `actions/dependency-review-action` with default config.

**Step 2: Commit**

```bash
git add .github/workflows/dependency-review.yml
git commit -m "ci: add dependency-review on PR"
```

---

## Task 10: CodeQL / security workflow

**Files:**

- Modify or create: `.github/workflows/security.yml`

**Step 1: Ensure CodeQL for Kotlin and JavaScript/TypeScript**

If security.yml exists, ensure it runs CodeQL for Kotlin (or Java) and for JavaScript/TypeScript (functions). If it doesn’t exist, create a minimal workflow: init + analyze for both language matrices, schedule and push to main.

**Step 2: Commit**

```bash
git add .github/workflows/security.yml
git commit -m "ci: CodeQL for Kotlin and JS/TS"
```

---

## Task 11: Root .prettierignore and verify scripts

**Files:**

- Create: `.prettierignore`
- Modify: `package.json` (root)

**Step 1: Create .prettierignore**

Add lines: `generated/`, `build/`, `**/build/`, `node_modules/`, `**/node_modules/`, `lib/`, `.gradle/`, `app/build/`, `feature-ai/build/`, `**/lib/`.

**Step 2: Add verify scripts to root package.json**

Add scripts:

- `"verify:android": "./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest :app:lintDebug detekt --no-daemon"`
- `"verify:functions": "cd functions && npm ci && npm run build && npm run lint && npm test"`
- `"verify": "npm run verify:android && npm run verify:functions && npm run format:check"`

Adjust detekt task name if different (e.g. `detekt` or `:app:detekt :feature-ai:detekt`).

**Step 3: Commit**

```bash
git add .prettierignore package.json
git commit -m "chore: add .prettierignore and verify scripts"
```

---

## Task 12: Update DEVELOPMENT.md and CONTRIBUTING.md

**Files:**

- Modify: `DEVELOPMENT.md`
- Modify: `docs/CONTRIBUTING.md`

**Step 1: DEVELOPMENT.md**

Add/update: Node 24 for functions, `functions/.nvmrc`, Prettier ignore, `npm run verify`, CI uses pinned action SHAs and Node 24 for functions.

**Step 2: CONTRIBUTING.md**

Add: Run `npm run verify` to check everything locally; hooks run equivalent checks.

**Step 3: Commit**

```bash
git add DEVELOPMENT.md docs/CONTRIBUTING.md
git commit -m "docs: DEVELOPMENT and CONTRIBUTING for hardening"
```

---

## Task 13: README verify and Node 24 mention

**Files:**

- Modify: `README.md`

**Step 1: Add verify and Node 24**

In "Build and verification", add: "Or run `npm run verify` to run Android build/tests/lint/detekt, functions build/lint/test, and format check." Mention "Functions: Node 24" in tech stack or Firebase setup.

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: README verify command and Node 24"
```

---

## Execution handoff

Plan saved to `docs/plans/2026-02-17-full-hardening-config.md`. Design: `docs/plans/2026-02-17-full-hardening-config-design.md`.

**Two execution options:**

1. **Subagent-Driven (this session)** — Use superpowers:subagent-driven-development; one subagent per task with code review between tasks.
2. **Parallel Session** — Open a new session in the worktree and use superpowers:executing-plans to run through the plan with checkpoints.

**Which approach?**

If Subagent-Driven: stay in this session, fresh subagent per task + code review.  
If Parallel Session: new session, execute plan task-by-task with checkpoints.
