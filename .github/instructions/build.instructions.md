# Build and CI Instructions

Applies to:
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/**`
- `.github/workflows/**`

1. Keep CI checks strict and meaningful (no silent pass-through for expected gates).
2. Use least-privilege permissions in workflows.
3. Keep module path filters aligned with real source layout.
4. Pin high-risk third-party actions to immutable SHAs.
