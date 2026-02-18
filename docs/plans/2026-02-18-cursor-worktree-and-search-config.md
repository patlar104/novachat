# Plan: Cursor Worktree and Search Configuration

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE and environment config)

## Goal

Make Cursor workspace/runtime setup consistent with NovaChat's current toolchain and verification workflow, with explicit support for worktree bootstrap and searchable assistant context.

## Steps

1. Align Cursor cloud runtime Node.js version with repo requirements (Node 24).
2. Improve worktree bootstrap so root and `functions/` dependencies are installed automatically.
3. Ensure Android SDK path is written when available so Gradle can resolve SDK in agent/worktree contexts.
4. Add Cursor-native MCP server configuration under `.cursor/` for project search/context tools.
5. Document required and optional secrets, plus Web Search and sandbox guidance in `.cursor/README.md`.
6. Re-run architecture-spec verification commands and report outcomes.

## Status

All steps implemented in this change set; verification results are reported in the task response.
