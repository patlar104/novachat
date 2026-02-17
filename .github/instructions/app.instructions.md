# App Module Instructions

Applies to: `app/**`

Architecture: see `specs/NOVACHAT_ARCHITECTURE_SPEC.md`.

1. Keep `app/` as composition root only (application bootstrap, theme host, navigation host, DI entrypoints).
2. Do not move feature/business logic into `app/`.
3. Verify navigation destinations match feature contracts.
4. Validate app-level changes with:
   - `./gradlew :app:assembleDebug`
