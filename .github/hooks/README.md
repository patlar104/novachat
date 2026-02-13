# Git Hooks for NovaChat

This directory contains repository-scoped hooks used for local quality gates.

## Hooks

## `pre-commit`

Runs scoped checks based on staged files:

1. Android/module changes: `./gradlew :feature-ai:testDebugUnitTest --no-daemon`
2. `functions/**` changes: `(cd functions && npm run build && npm run lint)`
3. `*.js|*.ts|*.json` changes: `npm run format:check`

No placeholder checks are used.

## `pre-push`

Runs full local gates:

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest --no-daemon
(cd functions && npm run build && npm run lint && npm test)
npm run format:check
```

## `commit-msg`

Enforces conventional commit message format:

```text
<type>(<scope>): <subject>
```

## Install

From repository root:

```bash
./.github/hooks/setup-hooks.sh
```

or configure directly:

```bash
git config core.hooksPath .github/hooks
```

## Bypass (temporary)

```bash
git commit --no-verify
git push --no-verify
```

## Disable

```bash
git config --unset core.hooksPath
```
