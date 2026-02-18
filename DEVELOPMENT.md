# Development Setup

- **Android:** Android Studio or CLI; see `gradle/libs.versions.toml` for versions. JDK 21. Add `app/google-services.json` for Firebase.
- **Functions:** Node 24 (see `functions/package.json` engines and `functions/.nvmrc`). Run `cd functions && npm install`. Set `GEMINI_API_KEY` (env or Firebase params) for local run. Emulator: `cd functions && npm run serve` (functions on port 5002; see `firebase.json`).
- **Data Connect (optional):** Emulator config in `firebase.json`; schema and connectors in `dataconnect/`. Generated clients in `generated/dataconnect/` — do not edit by hand.
- **Format/lint:** `npm run format:check` (root; respects `.prettierignore`); functions: `npm run lint` and `npm test`. Run **`npm run verify`** to run Android build/tests/lint/detekt, functions build/lint/test, and format check (matches CI locally).
- **CI:** Workflows use pinned action SHAs where noted; Functions CI uses Node 24. Update SHAs from each action’s README when upgrading.
- **Cursor/VS Code:** `.cursor/` and `.vscode/` hold environment and MCP config; see `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7.
