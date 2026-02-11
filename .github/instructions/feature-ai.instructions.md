# Feature-AI Instructions

Applies to: `feature-ai/**`

1. Keep domain/presentation/data separation explicit.
2. Use split UI model files under `presentation/model/chat`, `presentation/model/settings`, and `presentation/model/common`.
3. Use `core-network` for Firebase transport operations and `core-common` for shared error/result mapping.
4. Preserve OFFLINE capability scaffold behavior (state-driven, non-crashing).
5. Validate with:
   - `./gradlew :feature-ai:testDebugUnitTest`
