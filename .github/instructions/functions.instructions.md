# Functions Instructions

Applies to: `functions/**`

Architecture: see `specs/NOVACHAT_ARCHITECTURE_SPEC.md`.

1. Keep `functions/src/index.ts` as wiring/exports only.
2. Keep validation, error mapping, client calls, and analytics in dedicated modules.
3. Preserve explicit request/response typings.
4. Use the package manager for dependency changes (e.g. `npm install -D <pkg>@<version>` in `functions/`); avoid editing `package.json` by hand so the lockfile and resolution stay consistent.
5. Keep lint/build/test green:
   - `(cd functions && npm run build && npm run lint && npm test)`
