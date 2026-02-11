# Hilt Test Migration & Coverage Updates Spec

**Date**: February 10, 2026
**Status**: Draft
**Owner**: Planner Agent

## 1. Overview

This spec defines the scope and plan for migrating NovaChat tests to modern Hilt testing patterns and expanding coverage in unit and androidTest suites. The work is test-only and does not modify production code.

## 2. Scope

- Migrate existing tests to Hilt testing infrastructure where applicable.
- Add/update unit and androidTest coverage for Hilt-injected components.
- Ensure consistent usage of Hilt testing annotations, rules, and test runners.

## 3. Goals

- Use Hilt testing best practices in unit and instrumentation tests.
- Standardize test setup for dependency injection across modules.
- Improve coverage for ViewModels, repositories, and UI flows that rely on DI.
- Ensure tests are deterministic and isolated via fakes/mocks.

## 4. Non-Goals

- No production code changes.
- No build configuration changes unless strictly required by test-only updates.
- No feature additions or UI changes.

## 5. Affected Files (Tests Only)

> Final list determined during implementation discovery. Expected locations:

- `feature-ai/src/test/java/**` (unit tests)
- `feature-ai/src/androidTest/java/**` (instrumentation tests)
- `app/src/test/java/**` (unit tests)
- `app/src/androidTest/java/**` (instrumentation tests)

## 6. Architecture & Standards

- Follow MVVM + Clean Architecture testing boundaries.
- Use `Result<T>`-based assertions for error paths.
- Use `StateFlow`/`Channel`-driven ViewModel tests where applicable.
- Avoid real repositories in unit tests; use fakes/mocks.

## 7. Test Strategy

### 7.1 Unit Tests (JVM)

- **Targets**: ViewModels, UseCases, repository implementations with fakes.
- **Approach**:
  - Use MockK or fakes for repository interfaces.
  - Use coroutine test utilities (`runTest`, test dispatchers).
  - Validate state transitions for each `UiEvent` path.
  - Validate one-time effects via `Channel` collection.

### 7.2 Instrumentation Tests (androidTest)

- **Targets**: Compose UI screens and Hilt-injected Android components.
- **Approach**:
  - Use Hilt test runner and rules to inject test dependencies.
  - Provide test modules with `@TestInstallIn` when replacing bindings.
  - Use Compose UI testing APIs for UI assertions.
  - Use `HiltAndroidRule` for injection and setup order.

## 8. Hilt Testing Annotations & Rules

### 8.1 Required Annotations

- `@HiltAndroidTest` for instrumentation tests that require DI.
- `@UninstallModules` for replacing production modules in tests.
- `@TestInstallIn` for test-specific bindings.

### 8.2 Rules & Runners

- `HiltAndroidRule` must be the first rule executed when used.
- Use `HiltTestRunner` for instrumentation tests (already configured).
- Ensure `@get:Rule` ordering with Compose test rules if used.

## 9. Dependency Order (Execution Plan)

1. **Discovery**: Inventory existing tests and current DI usage.
2. **Unit Test Migration**: Update ViewModel and UseCase tests with consistent DI/fakes.
3. **androidTest Migration**: Update instrumentation tests to use Hilt rules and test modules.
4. **Coverage Expansion**: Add missing tests for error paths and edge cases.
5. **Validation**: Run unit + androidTest suites, confirm no regressions.

## 10. Acceptance Criteria

- [ ] All relevant tests use appropriate Hilt testing annotations/rules.
- [ ] Unit tests for DI-dependent classes use fakes/mocks (no real repositories).
- [ ] Instrumentation tests compile and run with `HiltTestRunner`.
- [ ] Coverage includes success and failure paths for DI-driven flows.
- [ ] No production code changes introduced.
- [ ] All updated tests pass (`:feature-ai:test`, `test`, and `connectedAndroidTest` when applicable).

## 11. Risks & Mitigations

- **Risk**: Missing DI bindings in tests.
  - **Mitigation**: Use `@TestInstallIn` or `@UninstallModules` with replacement modules.
- **Risk**: Rule ordering causing injection before Compose setup.
  - **Mitigation**: Enforce `HiltAndroidRule` as the first rule.

## 12. Handoff Plan

- **Testing Agent**: Implement test migrations and coverage updates per this spec.
- **Reviewer Agent**: Validate protocol compliance and coverage completeness.

---

**End of Spec**
