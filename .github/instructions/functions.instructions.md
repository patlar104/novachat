# Functions Instructions

Applies to: `functions/**`

1. Keep `functions/src/index.ts` as wiring/exports only.
2. Keep validation, error mapping, client calls, and analytics in dedicated modules.
3. Preserve explicit request/response typings.
4. Keep lint/build/test green:
   - `(cd functions && npm run build && npm run lint && npm test)`
