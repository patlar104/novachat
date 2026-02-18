# Development Setup

- **Android:** Android Studio or CLI; see `gradle/libs.versions.toml` for versions. JDK 21. Add `app/google-services.json` for Firebase.
- **Android SDK detection:** Run `npm run android:setup-sdk` (or `bash ./scripts/ensure-android-sdk.sh`) to auto-detect SDK paths and write `local.properties` before Gradle builds.
- **google-services sync:** Run `npm run android:setup-google-services` to auto-copy `app/google-services.json` from known local locations (or set `NOVACHAT_GOOGLE_SERVICES_JSON=/absolute/path/google-services.json`). `npm run verify:android` runs both setup steps automatically.
- **Extensions sync:** Run `npm run editor:extensions:sync` to install missing recommended extensions using local `cursor` CLI (fallback `code` CLI).
- **Stale git locks:** Run `npm run git:clear-stale-locks` to remove stale, unused git `*.lock` files before automation runs.
- **Cursor MCP stability:** Run `npm run cursor:mcp:docker:build` to prebuild Docker MCP runtime used by `.cursor/mcp.json`. Wrappers auto-fallback to local `npx` when Docker is unavailable.
- **Functions:** Node 24 (see `functions/package.json` engines and `functions/.nvmrc`). Run `cd functions && npm install`. Set `GEMINI_API_KEY` (env or Firebase params) for local run. Emulator: `cd functions && npm run serve` (functions on port 5002; see `firebase.json`).
- **Data Connect (optional):** Emulator config in `firebase.json`; schema and connectors in `dataconnect/`. Generated clients in `generated/dataconnect/` — do not edit by hand.
- **Format/lint:** `npm run format:check` (root; respects `.prettierignore`); functions: `npm run lint` and `npm test`. Run **`npm run verify`** to run Android build/tests/lint/detekt, functions build/lint/test, and format check (matches CI locally).
- **CI:** Workflows use pinned action SHAs where noted; Functions CI uses Node 24. Update SHAs from each action’s README when upgrading.
- **Cursor/VS Code:** `.cursor/` and `.vscode/` hold environment and MCP config; see `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7.
