# Plan: Android SDK Auto-Detection for Multi-Environment Gradle Builds

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE and environment config)

## Goal

Make Gradle Android commands work across local machines, Cursor/cloud, and worktrees without manual `local.properties` edits.

## Steps

1. Add a repo script that detects Android SDK from environment variables and common install paths.
2. Have the script update `local.properties` (`sdk.dir=...`) idempotently.
3. Wire this script into developer entry points that run Gradle checks (`npm run verify:android`, workspace tasks, hooks, Cursor worktree setup).
4. Document the setup command for contributors.
5. Re-run required verification commands and report outcomes.

## Status

Implemented in this change set. Verification results are reported with the task response.
