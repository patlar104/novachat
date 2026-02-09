````skill
---
name: dependency-injection
description: Complete manual DI patterns for NovaChat using AppContainer (NO placeholders)
category: architecture
applies_to:
  - "**/di/**/*.kt"
  - "**/AppContainer.kt"
  - "**/*ViewModelFactory.kt"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Dependency Injection (DI) Skill for NovaChat

This skill documents NovaChat's manual dependency injection pattern using `AppContainer`. No Hilt or Koin—just clean, explicit Kotlin lazy initialization.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete AppContainer with all dependencies
> - Complete ViewModel factory functions
> - All lazy singleton patterns shown
> - No placeholder dependencies

---

## Why Manual DI for NovaChat?

- ✅ **Explicit**: Dependencies visible in code, not magic annotations
- ✅ **Testable**: Easy to inject fakes/mocks in tests
- ✅ **Fast**: No annotation processing overhead
- ✅ **Simple**: One file manages all dependencies
- ✅ **Control**: Full control over initialization order

---

## AppContainer Pattern (Complete)

### Basic Structure

Rules:

- Keep DI wiring in [`di/AppContainer.kt`](../../app/src/main/java/com/novachat/app/di/AppContainer.kt).
- Use lazy singletons for data sources, repositories, and use cases.
- ViewModel factories live in `AppContainer` and accept `SavedStateHandle`.
- Composables access the container via `LocalContext.current.appContainer`.
- **Firebase Dependencies**: AppContainer provides Firebase Functions and Firebase Auth instances for repositories. These are initialized in NovaChatApplication and passed to repositories that need them (e.g., AiRepositoryImpl).


---

## Using AppContainer in Composables

### Step 1: Extend Application Class

Rules:

- Declare a custom `Application` class and initialize `AppContainer` in `onCreate()`.
- Keep a `lateinit var appContainer` for app-wide access.

### Step 2: Declare in Manifest

Rules:

- Set `android:name` to the custom `Application` class in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).

### Step 3: Create ViewModel Factory

Rules:

- Keep ViewModel factory in [`presentation/viewmodel/ViewModelFactory.kt`](../../app/src/main/java/com/novachat/app/presentation/viewmodel/ViewModelFactory.kt).
- Factory delegates ViewModel creation to `AppContainer` methods.

### Step 4: Provide AppContainer in MainActivity

Rules:

- Provide `LocalAppContainer` in `MainActivity` via `CompositionLocalProvider`.
- Use the `Application` instance to access `appContainer`.

### Step 5: Use in Composables

Rules:

- Access `LocalAppContainer.current` in Composables.
- Create ViewModels with `ViewModelFactory(appContainer)`.

---

## Dependency Graph

Rules:

- `AppContainer` owns repositories, use cases, and ViewModel factories.
- Repositories are singletons reused across ViewModels.
- UseCases depend on repositories and are singletons.
- **Firebase Dependencies**: Firebase Functions and Firebase Auth instances are provided to repositories (e.g., AiRepositoryImpl) that need them. These are initialized in NovaChatApplication before AppContainer creation.

---

## Lazy Initialization Pattern

### How It Works

Rules:

- Use `by lazy` for AppContainer dependencies.
- First access creates the instance; subsequent access returns cached instance.

### Benefits for Testing

Rules:

- Construct real `AppContainer` in production.
- Inject fake repositories/use cases in tests.

---

## Adding New Dependencies

### When Adding a New Feature (e.g., Login)

Rules:

- Add new repository interface in `domain/repository/` and implementation in `data/repository/`.
- Add new use case in `domain/usecase/` with required dependencies.
- Wire new repository/use case and factory method in `AppContainer`.
- Update `ViewModelFactory` to support the new ViewModel.

---

## Circular Dependency Prevention

### ❌ DON'T: Create Circular Dependencies

Rules:

- Dependencies must be acyclic; break cycles by extracting shared logic.

### ✅ DO: Verify Dependency Chart

Rules:

- Confirm repositories depend on data sources only.
- UseCases depend on repositories only.
- ViewModels depend on UseCases only.

---

## Protocol Compliance Checklist

Before submitting DI code, verify:

- [ ] **AppContainer complete** - All repositories, use cases, factories included
- [ ] **No circular dependencies** - Each dependency has clear direction
- [ ] **All factories implemented** - Every ViewModel has factory method
- [ ] **Lazy initialization used** - Dependencies only created when accessed
- [ ] **Imports correct** - No missing imports for all dependencies
- [ ] **Tested** - Fakes/mocks can be injected in tests
- [ ] **Documented** - Comments explain dependencies
- [ ] **No singletons bypassed** - Always use AppContainer, never static instances

**Remember: Clean DI makes testing and refactoring easy. Manual DI is explicit and testable!**

---

**End of Dependency Injection Skill**

````
