---
name: security-check
description: Complete security implementations for NovaChat (NO placeholders)
category: security
applies_to:
  - "**/*.kt"
  - "**/*.java"
  - "**/build.gradle*"
  - "**/AndroidManifest.xml"
protocol_compliance: true
note: All examples are COMPLETE implementations - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Security Best Practices Skill

This skill provides **COMPLETE** security implementations for Android development. All code examples are fully implemented with no placeholders.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete security implementations (no `// ... security code` placeholders)
> - All imports explicitly shown
> - Full encryption examples
> - Complete network security configurations
> - Full permission handling code

## Multi-Agent Coordination

### When Build Agent Should Use Tools (Security Context)

**Use tools immediately for:**
- Reading build.gradle.kts for dependency security → `read_file`
- Checking AndroidManifest.xml security config → `read_file`
- Searching for hardcoded secrets → `grep_search`
- Reviewing network security configuration → `read_file`
- Adding security dependencies → `apply_patch`
- Updating ProGuard/R8 rules → `apply_patch`
- Verifying OWASP, GitHub Advisory, security docs → **Cursor's built-in browser** (browser_navigate, browser_snapshot, browser_evaluate) - MANDATORY: This is the ONLY browser tool available. Do NOT use fetch or any other browser tools.
- Checking git status or diff for security-related changes → **GitKraken MCP** (git_status, git_log_or_diff)
- Finding older security-related edits from other IDEs → **Pieces MCP** (ask_pieces_ltm)

**Do NOT describe; DO implement:**
- Don't say "add secure encryption"; implement using `apply_patch`
- Don't say "configure network security"; create/update config using `create_file`
- Don't say "check for hardcoded secrets"; search and report using `grep_search`

### When Build Agent Should Hand Off

**Hand off to Backend Agent if:**
- Authentication system needs implementation
- DataStore encryption implementation needed
- Token storage implementation needed
- API key management needs implementation
- → **Action**: Report security requirement for backend to implement

**Hand off to UI Agent if:**
- Permission request UI needed
- Security-related dialogs needed
- Secure input handling UI needed
- → **Action**: Report UI security implementation needed

**Hand off to another specialist if:**
- SSL/Certificate pinning needs verification
- Encryption algorithm selection needs expertise
- Compliance requirements (GDPR, etc.) need addressing
- → **Action**: Report security requirement for specialist review

### Security Task Assessment

**Determine scope before acting:**

1. **Is this a security/build task?**
   - Configuring network security → YES, use Build Agent tools
   - Adding secure dependencies → YES, use Build Agent tools
   - Checking for hardcoded secrets → YES, use Build Agent tools
   - Implementing authentication → NO, hand off to Backend Agent
   - Creating permission UI → NO, hand off to UI Agent

2. **Do I have all context needed?**
   - What security requirements apply? → Review project guidelines
   - What vulnerabilities exist? → Search with `grep_search`
   - What dependencies are affected? → Check build.gradle.kts

3. **Is this within Build Agent scope?**
   - Configuring security settings in manifest → YES ✓
   - Managing secure dependencies → YES ✓
   - Setting up network security config → YES ✓
   - Implementing encryption logic → NO, hand off to Backend Agent
   - Creating secure UI components → NO, hand off to UI Agent
   - Writing security tests → NO, hand off to Testing Agent

## Critical Security Checks

### 1. Never Hardcode Secrets

Rules:

- Never hardcode secrets in source files.
- Use `BuildConfig` fields sourced from `local.properties` and configured in [`app/build.gradle.kts`](../../app/build.gradle.kts).

### 2. Secure Network Communication

Rules:

- Set `android:usesCleartextTraffic="false"` in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).
- If a network security config is needed, place it in `app/src/main/res/xml/` and set `android:networkSecurityConfig` in the manifest.

### 3. Prevent SQL Injection

Rules:

- Use parameterized queries (`:param` in Room or `?` placeholders in SQLite).
- Never concatenate user input into SQL strings.

### 4. Secure Data Storage

Rules:

- Store sensitive data using `EncryptedSharedPreferences` or encrypted DataStore.
- Never store secrets in plain `SharedPreferences`.

### 5. Validate User Input

Rules:

- Validate user input before processing.
- Accept only `http://` or `https://` URLs; reject other schemes.

### 6. Secure WebView Configuration

Rules:

- Disable file/content access and file URL access in WebView settings.
- Only enable JavaScript when required.
- Disable geolocation and database access unless required.

## Permission Handling

### Request Only Necessary Permissions

Rules:

- Declare only required permissions in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).

### Runtime Permission Handling

Rules:

- Use `ContextCompat.checkSelfPermission()` before accessing protected APIs.
- Show rationale with `shouldShowRequestPermissionRationale()` when needed.
- Request permissions with `ActivityResultLauncher`.

## Authentication & Authorization

### Secure Token Storage

Rules:

- Store tokens in `EncryptedSharedPreferences` (or encrypted DataStore).
- Provide `saveToken()`, `getToken()`, and `clearToken()` in a token manager.

### Secure Password Handling

Rules:

- Never log or store passwords in plain text.
- Clear sensitive values from memory after use when possible.

## ProGuard/R8 Rules for Security

Rules:

- Keep security‑critical classes from being removed.
- Strip `android.util.Log` calls in release builds.
- Keep source/line attributes for crash reports.

## Common Security Vulnerabilities

### 1. Insecure Data Leakage

Rules:

- Never log secrets or tokens.
- Log only non‑sensitive status messages.

### 2. Exported Components

Rules:

- Set `android:exported="false"` for components not meant for external access.
- Declare exported components intentionally in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).

### 3. Intent Security

Rules:

- Prefer explicit intents for internal navigation.
- Avoid implicit intents for sensitive actions.

## Dependency Security

### Check Dependencies for Vulnerabilities

Rules:

- Check GitHub Advisory Database, Snyk, and OWASP Dependency‑Check before adding deps using **Cursor's built-in browser** (MANDATORY: This is the ONLY browser tool available. Do NOT use fetch or any other browser tools).
- Keep dependencies at latest stable versions.

### Verify Dependency Sources

Rules:

- Use trusted repositories only (`google()`, `mavenCentral()`).
- Avoid adding unknown Maven repos.

## File Access Security

### Secure File Sharing

Rules:

- Never share `file://` URIs; use `FileProvider`.
- Grant read permissions via `Intent.FLAG_GRANT_READ_URI_PERMISSION`.

Rules:

- Configure `FileProvider` in [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml).
- Keep provider `android:exported="false"` and define `@xml/file_paths`.

## Certificate Pinning (Advanced)

Rules:

- Use certificate pinning only when required by policy.
- Keep pins scoped to specific hosts.

## Security Checklist

Before releasing:

- [ ] No hardcoded API keys, passwords, or secrets
- [ ] All network traffic uses HTTPS
- [ ] Sensitive data encrypted at rest
- [ ] SQL injection prevented (using Room or parameterized queries)
- [ ] Input validation implemented
- [ ] WebView configured securely (if used)
- [ ] Only necessary permissions requested
- [ ] Exported components properly protected
- [ ] No sensitive data in logs
- [ ] ProGuard/R8 enabled for release builds
- [ ] Dependencies checked for vulnerabilities
- [ ] File sharing uses FileProvider
- [ ] Authentication tokens stored securely

## Resources

- OWASP Mobile Security Testing Guide
- Android Security Best Practices (official docs)
- GitHub Advisory Database
- CWE (Common Weakness Enumeration)

**Verification**: Use **ONLY Cursor's built-in browser** (cursor-ide-browser MCP - the ONLY browser tool available) to verify these resources. Do NOT use fetch or any other browser tools. See [cursor-browser skill](../cursor-browser/SKILL.md). Use **GitKraken MCP** (git_status, git_log_or_diff) for git context. See [gitkraken-mcp skill](../gitkraken-mcp/SKILL.md). Use **Pieces MCP** (ask_pieces_ltm) for older security edits from other IDEs. See [pieces-mcp skill](../pieces-mcp/SKILL.md).

## Protocol Compliance Checklist

Before submitting security-related code, verify:

- [ ] **Complete implementations** - No `// ... security code` placeholders
- [ ] **All imports included** - Every security-related import explicitly listed
- [ ] **Encryption complete** - Full DataStore/encryption setup shown
- [ ] **Network config complete** - Complete network_security_config.xml
- [ ] **Permission handling complete** - Full runtime permission request code
- [ ] **Input validation complete** - All validation logic implemented
- [ ] **No hardcoded secrets** - All secrets in local.properties or secure storage
- [ ] **Error handling** - Security failures handled gracefully
- [ ] **Logging** - No sensitive data logged

**Remember: DEVELOPMENT_PROTOCOL.md prohibits placeholder code in ALL files, including security implementations!**

---

**End of Security Best Practices Skill**
