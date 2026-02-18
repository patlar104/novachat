# Plan: Extension Sync and Git Lock Hygiene

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE and environment config)

## Goal

Improve agent reliability by automatically installing missing recommended editor extensions and clearing stale git lock files before automation/build steps.

## Steps

1. Add a script that discovers recommended extensions from workspace config and installs missing ones via `cursor` or `code` CLI.
2. Add a safe stale-lock cleanup script that removes only old, unused git lock files.
3. Wire both scripts into package scripts and worktree setup.
4. Add workspace task and extension recommendations file for VS Code/Cursor parity.
5. Document usage and rerun required verification commands.

## Status

Implemented in this change set. Verification results are reported with the task response.
