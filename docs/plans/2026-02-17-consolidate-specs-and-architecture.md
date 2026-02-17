# Consolidate Specs and Architecture — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace multiple outdated specs with one architect-friendly spec, then remove old spec files and point the repo to the new doc so future work does not break existing logic.

**Architecture:** Single source of truth in `specs/NOVACHAT_ARCHITECTURE_SPEC.md` describing current backend (callable + HTTP pipeline), target streaming/App Check roadmap, repo layout, and tech stack. No behavior changes to app or functions; docs and file deletions only.

**Tech Stack:** Markdown specs; repo paths: `specs/`, `docs/plans/`, `README.md`.

---

## Task 1: Ensure docs/plans exists and new spec is in place

**Files:**

- Create (if missing): `docs/plans/.gitkeep` or keep `docs/plans/2026-02-17-consolidate-specs-and-architecture.md` as the only file (directory exists when file exists).
- Verify: `specs/NOVACHAT_ARCHITECTURE_SPEC.md` exists and contains §1–§7 (product summary, repo layout, backend current/target, app architecture, tech stack, spec history).

**Step 1: Confirm new spec exists**

From repo root:

```bash
test -f specs/NOVACHAT_ARCHITECTURE_SPEC.md && echo "OK" || echo "MISSING"
```

Expected: `OK`

**Step 2: Confirm docs/plans exists**

```bash
test -d docs/plans && echo "OK" || echo "MISSING"
```

Expected: `OK` (directory exists when this plan file exists).

**Step 3: Commit (if you created the spec or plan in this run)**

```bash
git add specs/NOVACHAT_ARCHITECTURE_SPEC.md docs/plans/2026-02-17-consolidate-specs-and-architecture.md
git status
git commit -m "docs: add NovaChat architecture spec and consolidate-specs plan"
```

---

## Task 2: Update README to reference the new spec

**Files:**

- Modify: `README.md` — in the "Active documentation" or equivalent section, add the single spec reference and remove or replace references to old specs.

**Step 1: Locate documentation section in README**

Open `README.md` and find the list that includes `DEVELOPMENT.md`, `API.md`, `docs/ARCHITECTURE.md`, `functions/README.md`.

**Step 2: Add spec reference**

Ensure a line exists that points to the canonical spec, for example:

```markdown
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` — architecture and product spec (single source of truth)
```

If README mentions "specs" or "SPEC" in a way that implies the old files, update that text to reference only `NOVACHAT_ARCHITECTURE_SPEC.md`.

**Step 3: Commit**

```bash
git add README.md
git commit -m "docs: point README to NovaChat architecture spec"
```

---

## Task 3: Remove outdated spec files

**Files:**

- Delete: `specs/spec_1_android_ai_app_tbd.md`
- Delete: `specs/PHASE_1_MODULARIZATION.md`
- Delete: `specs/KSP_MIGRATION.md`
- Delete: `specs/HILT_TEST_MIGRATION.md`

**Step 1: Delete first file**

```bash
rm -f specs/spec_1_android_ai_app_tbd.md
```

**Step 2: Delete remaining three**

```bash
rm -f specs/PHASE_1_MODULARIZATION.md specs/KSP_MIGRATION.md specs/HILT_TEST_MIGRATION.md
```

**Step 3: Verify only new spec remains**

```bash
ls specs/
```

Expected: `NOVACHAT_ARCHITECTURE_SPEC.md` (and any other files you did not delete; no `spec_1_android_ai_app_tbd.md`, `PHASE_1_MODULARIZATION.md`, `KSP_MIGRATION.md`, `HILT_TEST_MIGRATION.md`).

**Step 4: Commit**

```bash
git add -A specs/
git status
git commit -m "docs: remove outdated specs; single spec is specs/NOVACHAT_ARCHITECTURE_SPEC.md"
```

---

## Task 4: Search and fix references to old specs

**Files:**

- Modify: Any file under `.github/`, `docs/`, or root that references `spec_1_android_ai_app_tbd`, `PHASE_1_MODULARIZATION`, `KSP_MIGRATION`, or `HILT_TEST_MIGRATION` by name or path.

**Step 1: Find references**

From repo root:

```bash
rg -l "spec_1_android_ai_app_tbd|PHASE_1_MODULARIZATION|KSP_MIGRATION|HILT_TEST_MIGRATION" --glob '!node_modules' --glob '!*.lock' . || true
```

**Step 2: Update each hit**

For each path returned, replace or remove the reference so it points to `specs/NOVACHAT_ARCHITECTURE_SPEC.md` or to `docs/plans/` as appropriate. If the reference is in a historical/archive doc, you may add a one-line note that the spec was consolidated into `NOVACHAT_ARCHITECTURE_SPEC.md` instead of deleting the reference.

**Step 3: Commit**

```bash
git add -A
git status
git commit -m "docs: fix references to old specs"
```

---

## Execution handoff

Plan complete and saved to `docs/plans/2026-02-17-consolidate-specs-and-architecture.md`.

**Two execution options:**

1. **Subagent-Driven (this session)** — Dispatch a fresh subagent per task, review between tasks, fast iteration.

2. **Parallel Session (separate)** — Open a new session with executing-plans and run through the plan with checkpoints.

**Which approach?**

If Subagent-Driven is chosen: use superpowers:subagent-driven-development in this session (fresh subagent per task + code review).

If Parallel Session is chosen: open a new session in the worktree and use superpowers:executing-plans there.
