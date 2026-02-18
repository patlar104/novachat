# Detekt ReportingExtension Deprecation Fix

## Issue

Detekt 1.23.8 shows a deprecation warning:
```
The ReportingExtension.file(String) method has been deprecated.
This is scheduled to be removed in Gradle 10.
```

## Research Findings

### Current Status
- **Detekt 1.23.8** (current): Still uses deprecated `ReportingExtension.file()` API
- **Detekt 2.0.0-alpha.2** (latest): Fixed to use `baseDirectory.dir()` API

### Fix Location
The fix is in Detekt 2.0.0-alpha.2:
- **Issue**: [#8452](https://github.com/detekt/detekt/issues/8452) - "Gradle 9.1 (rc1) deprecation of ReportingExtension.file"
- **Status**: Fixed in 2.0.0-alpha.2
- **Code change**: Changed from `ReportingExtension.file("detekt")` to `baseDirectory.dir("detekt")`

### Upgrade Considerations

**Detekt 2.0.0-alpha.2 requires:**
1. Plugin ID change: `io.gitlab.arturbosch.detekt` → `dev.detekt`
2. Gradle properties:
   ```properties
   android.newDsl=false
   android.builtInKotlin=false
   ```
3. Breaking changes: Multiple rule renames and API changes (see [release notes](https://github.com/detekt/detekt/releases/tag/v2.0.0-alpha.2))

**Current Recommendation:**
- **Stay on 1.23.8** for now
- The deprecation is a **warning only**, not an error
- Detekt 2.0.0 is still in alpha and requires significant configuration changes
- Wait for Detekt 2.0.0 stable release or a 1.23.9 patch (if released)

### When to Upgrade

Upgrade to Detekt 2.0.0 when:
1. Stable release is available
2. All breaking changes are documented and understood
3. Project is ready to handle `android.builtInKotlin=false` requirement
4. All rule renames and API changes are addressed

### References
- [Detekt Issue #8452](https://github.com/detekt/detekt/issues/8452)
- [Detekt 2.0.0-alpha.2 Release Notes](https://github.com/detekt/detekt/releases/tag/v2.0.0-alpha.2)
- [Gradle 9.1 Upgrading Guide](https://docs.gradle.org/9.3.1/userguide/upgrading_version_9.html#reporting_extension_file)
