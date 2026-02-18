# Plan: Cursor Docker MCP Fallback

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE/environment config)

## Goal

Reduce Cursor tool hiccups by running MCP servers in Docker when available and auto-installing missing runtime dependencies inside that container.

## Steps

1. Add a dedicated Docker image for Cursor MCP tooling.
2. Add scripts that auto-build the image when missing.
3. Route Cursor MCP server commands through wrapper scripts (Docker first, local fallback).
4. Document overrides and fallback behavior.
5. Re-run required verification commands and report results.

## Status

Implemented in this change set. Verification results are reported with the task response.
