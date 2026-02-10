````skill
---
name: dependency-injection
description: Complete manual DI patterns for NovaChat using AiContainer (NO placeholders)
category: architecture
applies_to:
  - "**/di/**/*.kt"
  - "**/AiContainer.kt"
  - "**/*ViewModelFactory.kt"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Dependency Injection (DI) Skill for NovaChat

This skill documents NovaChat's manual dependency injection pattern using `AiContainer`. No Hilt or Koin—just clean, explicit Kotlin lazy initialization.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete AiContainer with all dependencies
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

## AiContainer Pattern (Complete)

### Basic Structure

Rules:

- Keep DI wiring in [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt).
- Use lazy singletons for data sources, repositories, and use cases.
- ViewModel factories accept `SavedStateHandle` and receive dependencies from `AiContainer`.
- Composables access the container via `LocalContext.current.aiContainer`.
- **Firebase Dependencies**: AiContainer provides Firebase Functions and Firebase Auth instances for repositories. These are initialized in NovaChatApplication and passed to repositories that need them (e.g., AiRepositoryImpl).


---

## Using AiContainer in Composables

### Step 1: Extend Application Class

Rules:

- Declare a custom `Application` class and initialize `AiContainer` in `onCreate()`.
- Implement `AiContainerProvider` and expose a `lateinit var aiContainer` for app-wide access.

### Step 2: Declare in Manifest

Rules:

- Set `android:name` to the custom `Application` class in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).

### Step 3: Create ViewModel Factory

Rules:

- Keep ViewModel factory in [`presentation/viewmodel/ViewModelFactory.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ViewModelFactory.kt).
- Factory receives `AiContainer` and provides ViewModels with required dependencies.

### Step 4: Provide AiContainer in MainActivity

Rules:

- Access `aiContainer` via `Context.aiContainer` in `MainActivity`.
- Pass `aiContainer` into `ViewModelFactory` when creating ViewModels.

### Step 5: Use in Composables

Rules:

- Access `LocalContext.current.aiContainer` in Composables.
- Create ViewModels with `ViewModelFactory(aiContainer)`.

---

## Dependency Graph

Rules:

- `AiContainer` owns repositories, use cases, and ViewModel factories.
- Repositories are singletons reused across ViewModels.
- UseCases depend on repositories and are singletons.
- **Firebase Dependencies**: Firebase Functions and Firebase Auth instances are provided to repositories (e.g., AiRepositoryImpl) that need them. These are initialized in NovaChatApplication before AiContainer creation.

---

## Lazy Initialization Pattern

### How It Works

Rules:

- Use `by lazy` for AiContainer dependencies.
- First access creates the instance; subsequent access returns cached instance.

### Benefits for Testing

Rules:

- Construct real `AiContainer` in production.
- Inject fake repositories/use cases in tests.

---

## Adding New Dependencies

### When Adding a New Feature (e.g., Login)

Rules:

- Add new repository interface in `domain/repository/` and implementation in `data/repository/`.
- Add new use case in `domain/usecase/` with required dependencies.
- Wire new repository/use case and factory method in `AiContainer`.
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

- [ ] **AiContainer complete** - All repositories, use cases, factories included
- [ ] **No circular dependencies** - Each dependency has clear direction
- [ ] **All factories implemented** - Every ViewModel has factory method
- [ ] **Lazy initialization used** - Dependencies only created when accessed
- [ ] **Imports correct** - No missing imports for all dependencies
- [ ] **Tested** - Fakes/mocks can be injected in tests
- [ ] **Documented** - Comments explain dependencies
- [ ] **No singletons bypassed** - Always use AiContainer, never static instances

**Remember: Clean DI makes testing and refactoring easy. Manual DI is explicit and testable!**

---

**End of Dependency Injection Skill**

````
