# NovaChat - Copilot Instructions

This is an Android application repository using Gradle as the build system. Please follow these guidelines when contributing:

## Code Standards

### Required Before Each Commit
- If code formatting tools are configured, run `./gradlew ktlintFormat` or `./gradlew spotlessApply` before committing changes
- Ensure all code follows Android and Kotlin best practices

### Development Flow
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
- Clean build: `./gradlew clean build`
- Check code quality: `./gradlew check`

## Repository Structure
- `app/`: Main Android application module
- `build/`: Build outputs (auto-generated, not committed)
- `.gradle/`: Gradle cache files (not committed)
- `gradle/`: Gradle wrapper files
- `.idea/`: Android Studio IDE configuration files (mostly not committed)

## Key Guidelines

1. **Follow Android Best Practices**
   - Follow Material Design guidelines for UI components
   - Use AndroidX libraries instead of legacy support libraries
   - Implement proper Activity/Fragment lifecycle management
   - Use ViewBinding or DataBinding instead of findViewById

2. **Kotlin Coding Standards**
   - Use Kotlin idiomatic patterns (data classes, sealed classes, extension functions)
   - Prefer immutability (val over var)
   - Use null-safety features properly
   - Follow Kotlin naming conventions

3. **Architecture**
   - Follow recommended Android app architecture (MVVM, MVI, or Clean Architecture)
   - Separate UI logic from business logic
   - Use dependency injection (Hilt/Dagger) if configured
   - Keep Activities/Fragments thin, move logic to ViewModels

4. **Testing**
   - Write unit tests for ViewModels and business logic
   - Write instrumentation tests for UI interactions
   - Use MockK or Mockito for mocking in tests
   - Aim for meaningful test coverage on critical paths

5. **Dependencies**
   - Keep dependencies up to date but stable
   - Avoid adding unnecessary dependencies
   - Check for security vulnerabilities before adding new dependencies

6. **Git Practices**
   - Never commit sensitive data (API keys, credentials, `google-services.json`)
   - Keep commits focused and atomic
   - Write clear commit messages
   - Don't commit build artifacts or IDE-specific files (already in `.gitignore`)

7. **Documentation**
   - Add KDoc comments for public APIs and complex logic
   - Update README.md when adding new features or changing setup instructions
   - Document any non-obvious architecture decisions

## Android-Specific Notes

- Minimum SDK version and target SDK version should be specified in build.gradle files
- Always test on multiple API levels if possible
- Handle configuration changes properly (screen rotation, etc.)
- Consider accessibility in UI implementations
- Follow Android security best practices for data storage and network communication
