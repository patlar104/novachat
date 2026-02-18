# NovaChat — Agent and Contributor Rules

1. **Architecture and product:** Use `specs/NOVACHAT_ARCHITECTURE_SPEC.md` as the single source of truth. Do not add product or backend contracts elsewhere without updating that spec.
2. **Path-scoped rules:** Follow `.github/instructions/*.instructions.md` for the paths you are editing (app, feature-ai, functions, build).
3. **Verification:** Before claiming work complete, run the commands in the spec §2 (Build and verification) and fix any failures.
4. **Plans:** New multi-step work goes in `docs/plans/YYYY-MM-DD-<name>.md` and should reference the architecture spec where relevant.
5. **Docs:** Do not add new top-level doc files (e.g. API.md, DEVELOPMENT.md) without listing them in README "Active documentation" and, if architectural, noting them in the spec. Do not add one-off investigation or branch-summary docs to `docs/plans/` unless the user explicitly requests them.
