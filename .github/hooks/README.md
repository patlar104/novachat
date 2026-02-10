# Git Hooks for NovaChat

This directory contains custom Git hooks to automate code quality checks and enforce standards.

## Available Hooks

### pre-commit

**Purpose**: Format code before committing

**What it does**:

- Finds all staged Kotlin files (`.kt`)
- Runs `ktlint` format on them
- Re-stages formatted files automatically
- Ensures code style consistency

**Skip with**: `git commit --no-verify`

---

### pre-push

**Purpose**: Run tests before pushing

**What it does**:

- Runs all unit tests (`testDebugUnitTest`)
- Prevents push if tests fail
- Shows test results summary

**Skip with**: `git push --no-verify`

---

### commit-msg

**Purpose**: Enforce conventional commit messages

**What it does**:

- Validates commit message format
- Enforces [Conventional Commits](https://www.conventionalcommits.org/) standard
- Ensures consistency in commit history

**Required format**:

```
<type>(<scope>): <subject>
```

**Valid types**:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements
- `ci`: CI/CD changes
- `build`: Build system changes
- `revert`: Reverting changes

**Examples**:

```
feat(chat): add message sending functionality
fix(ui): resolve button alignment issue
docs(readme): update installation instructions
refactor(viewmodel): simplify state management
test(repository): add unit tests for AI repository
```

**Skip with**: `git commit --no-verify`

---

## Installation

### Automatic Installation

Run the setup script from the repository root:

```bash
./.github/hooks/setup-hooks.sh
```

This will:

1. Make all hook scripts executable
2. Configure Git to use the `.github/hooks` directory
3. Create symlinks in `.git/hooks/` (optional)

### Manual Installation

Configure Git to use this directory for hooks:

```bash
git config core.hooksPath .github/hooks
```

Or create symlinks manually:

```bash
chmod +x .github/hooks/*
ln -s ../../.github/hooks/pre-commit .git/hooks/pre-commit
ln -s ../../.github/hooks/pre-push .git/hooks/pre-push
ln -s ../../.github/hooks/commit-msg .git/hooks/commit-msg
```

---

## Disabling Hooks

### Temporarily (for one commit/push)

```bash
git commit --no-verify
git push --no-verify
```

### Permanently

Unset the hooks path:

```bash
git config --unset core.hooksPath
```

Or remove the symlinks:

```bash
rm .git/hooks/pre-commit
rm .git/hooks/pre-push
rm .git/hooks/commit-msg
```

---

## Requirements

- **Bash**: All hooks are bash scripts
- **Gradle**: For running ktlint and tests
- **Git**: Version 2.9+ (for `core.hooksPath` support)

---

## Troubleshooting

### Hook not running

Check if hooks are executable:

```bash
ls -la .github/hooks/
```

Make them executable:

```bash
chmod +x .github/hooks/*
```

### ktlint not found

If ktlint is not configured in your Gradle build, the pre-commit hook will skip formatting with a warning.

To add ktlint, see the [ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle).

### Tests taking too long

The pre-push hook runs unit tests. To speed up:

1. Use `--no-verify` to skip (not recommended)
2. Configure Gradle daemon for faster builds
3. Run tests in parallel with Gradle

---

## CI/CD Integration

These hooks complement the GitHub Actions workflows in `.github/workflows/`:

- **android-ci.yml**: Full CI pipeline (build, test, lint)
- **security.yml**: Security scanning
- **code-quality.yml**: Code quality checks
- **release.yml**: Release automation

Local hooks provide fast feedback before pushing, while CI provides comprehensive validation.

---

## Contributing

To modify hooks:

1. Edit the hook script in `.github/hooks/`
2. Test locally
3. Commit changes
4. Others will get updates automatically (after running setup script)

---

## Notes

- Hooks are checked into version control
- All developers get the same hooks
- Hooks run locally, not on the server
- Can be bypassed with `--no-verify` (use with caution)
- CI workflows are the ultimate gatekeepers

---

**Last updated**: February 2026
