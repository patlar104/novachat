# Plan: VS Code and Cursor Settings Stability Pass

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE and environment config)

## Goal

Reduce intermittent tool/action hiccups by keeping MCP/sandbox/worktree behavior enabled while disabling unstable experimental agent flags.

## Steps

1. Audit workspace VS Code/Cursor settings for high-risk experimental toggles.
2. Keep core capabilities enabled (MCP discovery, sandbox terminal, web search integration).
3. Disable unsafe/experimental flags that commonly cause non-deterministic agent tool behavior.
4. Preserve repo-local docs with clear stability guidance.
5. Re-run required verification commands and report outcomes.

## Status

Implemented in this change set. Verification results are reported with the task response.
