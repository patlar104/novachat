# KSP Migration for Hilt (AGP 9 Built-in Kotlin)

**Date**: February 10, 2026
**Status**: Draft
**Owner**: Planner Agent

## Overview

Migrate annotation processing for Hilt from KAPT to KSP while keeping the AGP 9 built-in Kotlin setup intact. This spec defines the scope, goals, non-goals, affected build files, verification sources, dependency order, acceptance criteria, and rollback strategy. No production code changes are included in this spec.

## Scope

- Replace KAPT-based Hilt compiler configuration with KSP equivalents.
- Update Gradle plugin configuration to apply the KSP plugin where Hilt is used.
- Update dependency declarations from `kapt` to `ksp` for Hilt compiler.
- Keep AGP 9 built-in Kotlin configuration unchanged (no reintroducing `kotlin-android`).
- Update documentation/specs as needed (this spec only).

## Goals

- Eliminate KAPT usage for Hilt and use KSP instead.
- Ensure annotation processing works with AGP 9 built-in Kotlin support.
- Keep build configuration aligned with current 2026 standards and project architecture.
- Maintain clean, module-specific Gradle configurations.

## Non-Goals

- No changes to application code, DI graph, or Hilt annotations.
- No refactors to modules, build variants, or test suites.
- No changes to Firebase or other dependencies not related to Hilt/KSP.
- No plugin version upgrades beyond what is needed for KSP.

## Affected Build Files (Expected)

> **Note**: Final file list confirmed during implementation.

- `/Users/patrick/dev/projects/novachat/gradle/libs.versions.toml`
  - Add KSP plugin version + alias.
  - Ensure Hilt compiler dependency entry supports KSP usage.
- `/Users/patrick/dev/projects/novachat/app/build.gradle.kts`
  - Replace `kapt` configuration for Hilt compiler with `ksp`.
  - Apply KSP plugin alias.
  - Remove `id("org.jetbrains.kotlin.kapt")` if no other KAPT usage exists.
- `/Users/patrick/dev/projects/novachat/feature-ai/build.gradle.kts`
  - Apply KSP plugin alias.
  - Replace KAPT Hilt compiler usage with KSP if present.
- `/Users/patrick/dev/projects/novachat/core-common/build.gradle.kts`
  - Apply KSP plugin alias if Hilt is used here.
- `/Users/patrick/dev/projects/novachat/core-network/build.gradle.kts`
  - Apply KSP plugin alias if Hilt is used here.
- `/Users/patrick/dev/projects/novachat/build.gradle.kts`
  - Confirm plugin management/versions compatibility (if needed).

## Verification Sources (Maven Central Metadata)

Use Maven Central metadata URLs to verify versions during implementation:

- **Hilt Compiler**: `https://repo1.maven.org/maven2/com/google/dagger/hilt-compiler/maven-metadata.xml`
- **Hilt Android** (for reference): `https://repo1.maven.org/maven2/com/google/dagger/hilt-android/maven-metadata.xml`
- **KSP Gradle Plugin**: `https://repo1.maven.org/maven2/com/google/devtools/ksp/com.google.devtools.ksp.gradle.plugin/maven-metadata.xml`

> **Protocol Reminder**: Choose a web verification tool before fetching or validating any external sources.

## Dependency Order (Implementation Sequence)

1. **Version Catalog** (`gradle/libs.versions.toml`)
   - Add/update KSP plugin version and alias.
   - Ensure Hilt compiler dependency entry exists for KSP usage.
2. **Module Build Files**
   - Apply KSP plugin alias in each module that uses Hilt.
   - Replace `kapt` dependencies with `ksp` equivalents.
   - Remove KAPT plugin when no longer used.
3. **Root Build File** (if required)
   - Confirm plugin repositories and compatibility for KSP.

## Acceptance Criteria

- [ ] No `kapt` configuration remains for Hilt in module build files.
- [ ] KSP plugin applied only in modules that need it.
- [ ] Hilt compiler dependency declared with `ksp` where applicable.
- [ ] Build configuration remains compatible with AGP 9 built-in Kotlin.
- [ ] `./gradlew test` and `./gradlew assembleDebug` succeed after migration.
- [ ] No changes to production Kotlin source files.

## Rollback Plan

If migration fails or introduces build regressions:

1. Revert KSP plugin additions in affected module build files.
2. Restore `kapt` dependencies for Hilt compiler.
3. Re-enable KAPT plugin (`id("org.jetbrains.kotlin.kapt")`) where required.
4. Revert any version catalog changes for KSP.
5. Re-run `./gradlew test` to confirm recovery.

## Notes & Risks

- Ensure KSP plugin version is compatible with Kotlin 2.2.21 and AGP 9.
- Some modules may still require KAPT for other processors; verify before removal.
- Keep changes scoped to build configuration only; avoid code changes.
