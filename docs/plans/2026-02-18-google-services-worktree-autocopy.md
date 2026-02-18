# Plan: Auto-Copy google-services.json into Worktrees

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE and environment config)

## Goal

Automatically populate `app/google-services.json` in a new or existing worktree when an existing local copy is available.

## Steps

1. Add a script that validates and copies `google-services.json` from configured and common local locations.
2. Support explicit override via environment variable to avoid ambiguous source selection.
3. Wire the script into Android verification and worktree/bootstrap flows.
4. Document usage and fallback behavior.
5. Re-run required verification commands and report outcomes.

## Status

Implemented in this change set. Verification results are reported with the task response.
