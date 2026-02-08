# CI/CD and Automation Setup for NovaChat

This document describes the complete CI/CD pipeline and automation setup for the NovaChat Android application.

## Overview

The repository includes:

- **GitHub Actions Workflows**: Automated CI/CD pipelines
- **Git Hooks**: Local validation before commit/push
- **Dependabot**: Automated dependency updates
- **Security Scanning**: Vulnerability detection

---

## GitHub Actions Workflows

Located in `.github/workflows/`

### 1. Android CI (`android-ci.yml`)

**Triggers**:

- Push to `main` or `copilot/**` branches
- Pull requests to `main` or `copilot/**` branches
- Changes to app code or build files

**Jobs**:

- **Build & Test**: Builds debug APK, runs unit tests, lint checks
- **Instrumentation Tests**: Runs Android instrumentation tests on emulator (PRs only)

**Features**:

- Gradle caching for faster builds
- Parallel job execution
- Artifact uploads (APKs, test results, lint reports)
- PR comment with build status

**Configuration**:

- Runs on: `ubuntu-latest` (build), `macos-latest` (instrumentation)
- JDK: 17 (Temurin distribution)
- Timeout: 30 minutes (build), 45 minutes (instrumentation)

---

### 2. Security Scanning (`security.yml`)

**Triggers**:

- Push to `main` or `copilot/**` branches
- Pull requests
- Weekly schedule (Mondays at 00:00 UTC)

**Jobs**:

1. **Dependency Check**: Scans dependencies for known vulnerabilities
2. **CodeQL Analysis**: Static security analysis for Java/Kotlin code
3. **Secret Scanning**: Detects accidentally committed secrets with TruffleHog
4. **Gradle Security**: Checks for outdated dependencies

**Features**:

- Security alerts integration
- Automated vulnerability reports
- Weekly scheduled scans

---

### 3. Code Quality (`code-quality.yml`)

**Triggers**:

- Push to `main` or `copilot/**` branches with Kotlin file changes
- Pull requests with Kotlin file changes

**Jobs**:

1. **ktlint**: Kotlin code style checking
2. **Detekt**: Static code analysis for Kotlin
3. **Android Lint**: Android-specific lint checks
4. **Code Coverage**: Test coverage reports

**Features**:

- Automated lint issue annotations on PRs
- Quality gate enforcement
- Coverage tracking

---

### 4. Release Build (`release.yml`)

**Triggers**:

- Git tags matching `v*.*.*` (e.g., `v1.0.0`)
- Manual workflow dispatch with version input

**Jobs**:

1. **Build Release**: Creates signed APK and AAB
2. **Upload to Play Store**: Automated deployment (optional)

**Features**:

- Signed release builds with keystore secrets
- GitHub Release creation with draft
- Google Play Store upload support
- Release notes generation

**Required Secrets**:

- `RELEASE_KEYSTORE_BASE64`: Base64-encoded keystore file
- `RELEASE_KEYSTORE_PASSWORD`: Keystore password
- `RELEASE_KEY_ALIAS`: Key alias
- `RELEASE_KEY_PASSWORD`: Key password
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`: Play Store service account (optional)

---

## Dependabot (`dependabot.yml`)

**Configuration**:

- **Gradle dependencies**: Weekly updates on Mondays
- **GitHub Actions**: Weekly updates on Mondays
- **Grouped updates**: AndroidX, Compose, Kotlin, Testing libraries

**Features**:

- Automatic PR creation for updates
- Security update prioritization
- Grouped dependency updates
- Auto-assignment to reviewer

---

## Git Hooks

Located in `.githooks/`

### Available Hooks

1. **pre-commit**
   - Formats Kotlin files with ktlint
   - Auto-stages formatted files
   - Runs on: Every commit

2. **pre-push**
   - Runs unit tests
   - Prevents push if tests fail
   - Runs on: Every push

3. **commit-msg**
   - Enforces Conventional Commits format
   - Validates commit message structure
   - Runs on: Every commit

### Installation

Run the setup script:

```bash
./.githooks/setup-hooks.sh
```

This configures Git to use the `.githooks` directory automatically.

### Bypassing Hooks

When necessary (not recommended):

```bash
git commit --no-verify
git push --no-verify
```

See `.githooks/README.md` for detailed documentation.

---

## Workflow Execution Flow

### On Pull Request

```text
PR Created/Updated
  ↓
Android CI (build & test)
  ↓
Security Scanning
  ↓
Code Quality Checks
  ↓
Instrumentation Tests (if enabled)
  ↓
PR Status Check ✅/❌
```

### On Push to Main

```text
Code Pushed
  ↓
Android CI (build & test)
  ↓
Security Scanning
  ↓
Code Quality Checks
  ↓
All Checks Pass ✅
```

### On Release Tag

```text
Tag Created (v1.0.0)
  ↓
Release Build Workflow
  ↓
Build Signed APK/AAB
  ↓
Create GitHub Release (draft)
  ↓
Upload to Play Store (optional)
```

---

## Configuration for Your Repository

### 1. Enable GitHub Actions

Already enabled if you can see the Actions tab.

### 2. Configure Branch Protection (Recommended)

For `main` branch:

1. Go to Settings → Branches
2. Add rule for `main`
3. Enable:
   - Require status checks to pass before merging
   - Require branches to be up to date
   - Select required checks:
     - `Build & Test`
     - `Dependency Security Scan`
     - `CodeQL Security Analysis`

### 3. Set Up Release Secrets

For signed releases, add these secrets in Settings → Secrets and variables → Actions:

```bash
# Generate base64-encoded keystore
base64 -i your-release-keystore.jks | pbcopy

# Add as secret: RELEASE_KEYSTORE_BASE64
```

Required secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Optional (for Play Store):

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

### 4. Enable Dependabot

Already configured in `.github/dependabot.yml`. Dependabot will:

- Create PRs for dependency updates weekly
- Group related updates
- Assign to reviewers

### 5. Install Git Hooks (Local Development)

Each developer should run:

```bash
./.githooks/setup-hooks.sh
```

This ensures local validation before pushing.

---

## Monitoring and Maintenance

### Viewing Workflow Runs

1. Go to the **Actions** tab in your GitHub repository
2. Select a workflow to see recent runs
3. Click on a run to see detailed logs

### Troubleshooting Failed Workflows

1. Check the workflow logs in the Actions tab
2. Look for red ❌ marks indicating failures
3. Review error messages and stack traces
4. Fix the issue and push again (or re-run the workflow)

### Updating Workflows

Workflows are stored in `.github/workflows/`. To update:

1. Edit the YAML file
2. Commit and push
3. Workflow will use the new configuration on next run

---

## Best Practices

### For Developers

1. ✅ Run `.githooks/setup-hooks.sh` after cloning
2. ✅ Let hooks run (don't use `--no-verify` unless necessary)
3. ✅ Fix issues locally before pushing
4. ✅ Use conventional commit messages
5. ✅ Review Dependabot PRs promptly

### For Reviewers

1. ✅ Wait for CI checks to pass before reviewing
2. ✅ Check artifact uploads (lint reports, test results)
3. ✅ Review security scan results
4. ✅ Ensure code coverage is maintained

### For Releases

1. ✅ Test thoroughly before creating a release tag
2. ✅ Use semantic versioning (v1.0.0, v1.1.0, v2.0.0)
3. ✅ Review the generated release notes
4. ✅ Publish the draft release after verification

---

## Customization

### Adjusting Workflow Triggers

Edit the `on:` section in workflow files:

```yaml
on:
  push:
    branches: [ main, develop ]  # Add/remove branches
  schedule:
    - cron: '0 0 * * 1'  # Change schedule
```

### Adding New Workflow Jobs

Add a new job in the workflow file:

```yaml
jobs:
  my-custom-job:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: echo "Custom job"
```

### Modifying Hook Behavior

Edit scripts in `.githooks/`:

- Comment out checks you don't need
- Add new validation steps
- Adjust error messages

---

## Integration with copilot/create-ai-chatbot-app

The workflows are designed to work automatically when the `app/` directory is present. They will:

1. **Detect** if the app exists
2. **Skip** checks if no app directory found
3. **Run** full CI/CD when app code changes

This means:

- Workflows work on both configuration branches and app branches
- No manual updates needed when merging branches
- Configurations remain in sync automatically

---

## Performance Optimization

### Gradle Caching

All workflows use Gradle caching:

- First run: ~5-10 minutes (cold cache)
- Subsequent runs: ~2-3 minutes (warm cache)

### Parallel Execution

Jobs run in parallel when possible:

- Build + Security + Quality checks run simultaneously
- Reduces total CI time

### Selective Triggering

Workflows only run when relevant files change:

- `android-ci.yml`: Only on app code changes
- `code-quality.yml`: Only on Kotlin file changes
- Saves CI minutes

---

## Cost Considerations

### GitHub Actions Minutes

- **Public repositories**: Unlimited CI minutes
- **Private repositories**: 2,000 minutes/month (free tier)

### Tips to Reduce Usage

1. Use path filters to skip unnecessary runs
2. Cancel redundant workflow runs
3. Optimize Gradle builds with caching
4. Run expensive checks (instrumentation tests) only on PRs

---

## Troubleshooting Common Issues

### "gradlew not found"

Workflows check for `gradlew` and skip if not found. Add it to your repository:

```bash
gradle wrapper
git add gradlew gradle/wrapper/
git commit -m "Add Gradle wrapper"
```

### "ktlint not configured"

Add ktlint to `app/build.gradle.kts`:

```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}
```

### "Tests failing in CI but passing locally"

Common causes:

- Different JDK versions (CI uses JDK 17)
- Missing environment variables
- Timezone/locale differences

Fix: Ensure local environment matches CI (JDK 17, UTF-8, etc.)

---

## Support and Documentation

- **GitHub Actions**: <https://docs.github.com/actions>
- **Gradle**: <https://docs.gradle.org>
- **ktlint**: <https://pinterest.github.io/ktlint/>
- **Detekt**: <https://detekt.dev/>
- **Conventional Commits**: <https://www.conventionalcommits.org/>

---

**Last Updated**: February 2026
**Maintained By**: NovaChat Development Team
