# Contributing to NovaChat

- **Architecture and contracts:** See `specs/NOVACHAT_ARCHITECTURE_SPEC.md`.
- **Agent/AI rules:** See `AGENTS.md`.
- **Build and verify:** From repo root run **`npm run verify`** to run Android build, unit tests, lint, Detekt, functions build/lint/test, and format check. Or run the individual commands (see README “Build and verification”).
- **Git hooks (optional):** Install with `./.github/hooks/setup-hooks.sh` or `git config core.hooksPath .github/hooks`. Hooks run equivalent checks; see `.github/hooks/README.md`.
- **Commits:** Prefer conventional commits: `type(scope): subject` (e.g. `feat(functions): add rate limit to aiProxy`).
