# Plan: Add Validated AI Feature Settings (VS Code + Cursor)

Date: 2026-02-18

References:
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2 (build/verify commands)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7 (IDE/environment config)
- VS Code Copilot settings reference
- Cursor MCP docs

## Goal

Enable currently available AI features using only officially documented settings and schema fields, avoiding undocumented keys.

## Steps

1. Verify official settings names from VS Code Copilot settings reference.
2. Verify Cursor MCP schema fields from Cursor docs.
3. Update workspace settings to include validated agent/MCP toggles and safe terminal approval defaults.
4. Keep docs pointing to the source references.
5. Re-run required verification commands and report outcomes.

## Status

Implemented in this change set. Verification results are reported with the task response.
