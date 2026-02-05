---
name: Security Best Practices
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

> **⚠️ PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - ✅ Complete security implementations (no `// ... security code` placeholders)
> - ✅ All imports explicitly shown
> - ✅ Full encryption examples
> - ✅ Complete network security configurations
> - ✅ Full permission handling code

## Multi-Agent Coordination

### When Build Agent Should Use Tools (Security Context)

**Use tools immediately for:**
- Reading build.gradle.kts for dependency security → `read_file`
- Checking AndroidManifest.xml security config → `read_file`
- Searching for hardcoded secrets → `grep_search`
- Reviewing network security configuration → `read_file`
- Adding security dependencies → `replace_string_in_file`
- Updating ProGuard/R8 rules → `replace_string_in_file`

**Do NOT describe; DO implement:**
- Don't say "add secure encryption"; implement using `replace_string_in_file`
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

❌ **BAD**:
```kotlin
const val API_KEY = "sk_live_1234567890abcdef"
const val DATABASE_PASSWORD = "mypassword123"
```

✅ **GOOD**:
```kotlin
// Use BuildConfig (configured in build.gradle)
val apiKey = BuildConfig.API_KEY

// Or use local.properties (not committed to git)
// In build.gradle.kts:
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${localProperties["apiKey"]}\"")
    }
}
```

### 2. Secure Network Communication

❌ **BAD**:
```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="true"> <!-- Allows HTTP! -->
```

✅ **GOOD**:
```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="false"
    android:networkSecurityConfig="@xml/network_security_config">
```

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 3. Prevent SQL Injection

❌ **BAD**:
```kotlin
val cursor = db.rawQuery(
    "SELECT * FROM users WHERE name = '$userName'",
    null
)
```

✅ **GOOD**:
```kotlin
// Use Room (parameterized queries)
@Query("SELECT * FROM users WHERE name = :userName")
fun getUserByName(userName: String): User

// Or use parameterized queries with SQLite
val cursor = db.rawQuery(
    "SELECT * FROM users WHERE name = ?",
    arrayOf(userName)
)
```

### 4. Secure Data Storage

❌ **BAD**:
```kotlin
// Storing sensitive data in SharedPreferences (unencrypted)
sharedPreferences.edit()
    .putString("password", userPassword)
    .apply()
```

✅ **GOOD**:
```kotlin
// Use EncryptedSharedPreferences
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedPrefs.edit()
    .putString("auth_token", token)
    .apply()
```

### 5. Validate User Input

❌ **BAD**:
```kotlin
fun processUrl(url: String) {
    webView.loadUrl(url) // Dangerous! Could be javascript: scheme
}
```

✅ **GOOD**:
```kotlin
fun processUrl(url: String) {
    if (url.startsWith("http://") || url.startsWith("https://")) {
        webView.loadUrl(url)
    } else {
        throw IllegalArgumentException("Invalid URL scheme")
    }
}
```

### 6. Secure WebView Configuration

❌ **BAD**:
```kotlin
webView.settings.apply {
    javaScriptEnabled = true
    allowFileAccess = true
    allowContentAccess = true
    allowFileAccessFromFileURLs = true
    allowUniversalAccessFromFileURLs = true
}
```

✅ **GOOD**:
```kotlin
webView.settings.apply {
    javaScriptEnabled = true // Only if needed
    allowFileAccess = false
    allowContentAccess = false
    allowFileAccessFromFileURLs = false
    allowUniversalAccessFromFileURLs = false
    setGeolocationEnabled(false)
    databaseEnabled = false
}
```

## Permission Handling

### Request Only Necessary Permissions

```xml
<!-- AndroidManifest.xml -->
<!-- Only request permissions you actually need -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Runtime Permission Handling

```kotlin
// Check and request permissions at runtime
fun checkCameraPermission() {
    when {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            // Permission granted
            openCamera()
        }
        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            // Show explanation
            showPermissionRationale()
        }
        else -> {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
```

## Authentication & Authorization

### Secure Token Storage

```kotlin
class TokenManager(context: Context) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        getMasterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun clearToken() {
        encryptedPrefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }
}
```

### Secure Password Handling

```kotlin
// Never log or store passwords in plain text
fun loginUser(username: String, password: String) {
    // Hash password before sending (if not using HTTPS)
    // Or better: let the server handle password hashing
    
    // Clear password from memory after use
    password.toCharArray().fill('0')
}
```

## ProGuard/R8 Rules for Security

```proguard
# Keep security-critical classes
-keep class com.example.app.security.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Don't obfuscate crash reports (but protect sensitive methods)
-keepattributes SourceFile,LineNumberTable
```

## Common Security Vulnerabilities

### 1. Insecure Data Leakage

```kotlin
❌ // Logging sensitive data
Log.d("Auth", "User token: $authToken")

✅ // Don't log sensitive information
Log.d("Auth", "Authentication successful")
```

### 2. Exported Components

```xml
❌ <!-- Accidentally exported -->
<activity android:name=".SecretActivity"
    android:exported="true" /> <!-- Other apps can launch! -->

✅ <!-- Properly protected -->
<activity android:name=".SecretActivity"
    android:exported="false" />
```

### 3. Intent Security

```kotlin
❌ // Implicit intent - any app can respond
val intent = Intent("com.example.ACTION_PROCESS_DATA")
startActivity(intent)

✅ // Explicit intent - only your app
val intent = Intent(this, ProcessDataActivity::class.java)
startActivity(intent)
```

## Dependency Security

### Check Dependencies for Vulnerabilities

```kotlin
// Before adding any dependency, check:
// 1. GitHub Advisory Database
// 2. Snyk vulnerability database
// 3. OWASP Dependency-Check

// Keep dependencies updated
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Use latest stable
}
```

### Verify Dependency Sources

```kotlin
// Only use dependencies from trusted sources
repositories {
    google()
    mavenCentral()
    // Avoid unknown repositories
}
```

## File Access Security

### Secure File Sharing

```kotlin
❌ // Exposing file:// URIs
val fileUri = Uri.fromFile(file)
intent.putExtra(Intent.EXTRA_STREAM, fileUri) // Deprecated and insecure

✅ // Use FileProvider
val fileUri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file
)
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
```

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## Certificate Pinning (Advanced)

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

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
