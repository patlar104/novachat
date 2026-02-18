# Full Hardening Config — Design

**Date:** 2026-02-17  
**Status:** Approved  
**Scope:** Android + Functions + CI + root tooling (Approach 3)

---

## 1. Goal

Fix and update the entire NovaChat project so configs follow best practices: single source of truth for versions, stricter lint and type checks, CI hardening (pinned actions, dependency-review, security scanning), and one-command local verification.

---

## 2. Android config and hardening

- **Version source:** `gradle/libs.versions.toml` is the single source of truth. README and spec §5 reference it (or list agp/kotlin/compose-bom from it).
- **Docs:** README version text (e.g. Kotlin) aligned with `libs.versions.toml`.
- **Detekt:** Add Detekt with baseline; conservative rule set in `config/detekt.yml`. Run in CI (e.g. `./gradlew detekt`). Baseline generated and committed so current code passes.
- **Android Lint:** Keep existing `:app:lintDebug` in CI; no new lintOptions unless minimal (e.g. Critical-as-error).
- **Gradle:** No new plugins beyond Detekt; JDK 21 and wrapper unchanged.

---

## 3. Functions config and hardening

- **Node:** **Node 24** everywhere: `functions/package.json` engines `"node": "24"`, `functions/.nvmrc` with `24`, `functions/README.md` "Node.js 24", CI uses Node 24.
- **TypeScript:** `strict: true`; add `noUncheckedIndexedAccess` only if feasible without large changes; otherwise leave off.
- **ESLint:** Stricter rules or strictTypeChecked subset; fix new violations so CI stays green.
- **Security:** Optional `npm audit --audit-level=high` in CI and optional `"audit"` script in functions.

---

## 4. CI and security

- **Action pins:** Pin major actions to full SHA in all workflows (checkout, setup-java, setup-node, upload-artifact, gradle/wrapper-validation-action, etc.).
- **Functions CI:** Node 24 (single version; no matrix).
- **Dependency review:** Add `actions/dependency-review-action` on pull_request to main; default config.
- **CodeQL:** Kotlin/Java + JavaScript/TypeScript; schedule (push to main + weekly) and optionally on PR. Align with existing `security.yml` if present.
- **Docs:** Note in CONTRIBUTING or DEVELOPMENT that CI uses pinned SHAs and Node 24 for functions.

---

## 5. Root tooling and docs

- **.prettierignore:** Add at root; ignore `generated/`, `build/`, `**/build/`, `node_modules/`, `lib/`, `.gradle/`, `app/build/`, `feature-ai/build/`, etc.
- **Verify script:** Root `package.json`: `verify`, `verify:android`, `verify:functions`; `verify:android` runs assemble + unit tests + lint + detekt; `verify:functions` runs build + lint + test (and optional audit). `verify` runs both plus `format:check`.
- **Docs:** README (versions, Node 24, `npm run verify`); spec §5 (version sources, Node 24); DEVELOPMENT.md (Node 24, .nvmrc, Prettier ignore, verify, CI pins); CONTRIBUTING.md (verify, hooks).

---

## 6. Out of scope (this design)

- New features or product changes.
- Changing app or functions behavior beyond config/lint.
- Additional security tools (e.g. Snyk) or Node version matrix.
- Kotlin/Compose or TypeScript major upgrades beyond current catalog/engines.
