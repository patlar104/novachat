# Firebase AI Logic Migration Plan

Migration from deprecated `com.google.ai.client.generativeai` to Firebase AI Logic for NovaChat.

**Completed:** Firebase project, Android app registration, `google-services.json`, Gemini Developer API, `firebase init`.

---

## Remaining Steps

### 1. Gradle — Replace SDK & Add Firebase AI

**App `build.gradle.kts`:**

- Remove: `implementation("com.google.ai.client.generativeai:generativeai:0.9.0")`
- Add:

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
implementation("com.google.firebase:firebase-ai")
```

---

### 2. Initialize Firebase in Application

In `NovaChatApplication.kt`:

```kotlin
import com.google.firebase.FirebaseApp

class NovaChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // ...
    }
}
```

---

### 3. Rewrite `AiRepositoryImpl.kt`

Replace the old SDK with Firebase AI Logic:

**Remove:**
```kotlin
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

val generativeModel = GenerativeModel(
    modelName = AiMode.DEFAULT_MODEL_NAME,
    apiKey = apiKey.value,
    generationConfig = generationConfig { ... }
)
val response = generativeModel.generateContent(message)
```

**Add:**
```kotlin
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig

// Use FirebaseApp.getInstance() — NOT Firebase.app (KTX extension removed from Firebase BOM v34+)
val firebaseAI = FirebaseAI.getInstance(FirebaseApp.getInstance(), GenerativeBackend.googleAI())
val model = firebaseAI.generativeModel(
    modelName = "gemini-2.5-flash",
    generationConfig = generationConfig { ... }
)
val response = model.generateContent(message)
```

**Differences:**
- No API key in app — Firebase handles it
- Model: `gemini-2.5-flash` (replaces `gemini-1.5-flash`)
- `generateContent()` remains a suspend function in Kotlin
- Use `FirebaseApp.getInstance()` for the default app — `Firebase.app` is a KTX extension removed from Firebase BOM v34.0.0

---

### 4. Update `AiConfiguration` / `AiMode`

- Set `DEFAULT_MODEL_NAME` to `"gemini-2.5-flash"`
- With Gemini Developer API, online mode can omit user API key — Firebase stores it

---

### 5. API Key in Settings (Optional)

For Gemini Developer API, user API key is not required. You can:

- **Remove** API key from Settings, DataStore, and `AiConfiguration` for online mode
- **Keep** if you want users to provide their own key (e.g. for Vertex AI backend)

---

### 6. Adjust Tests

Update or remove tests that depend on the old SDK or `ApiKey`.

---

## Checklist

- [x] Firebase project
- [x] Android app + `google-services.json`
- [x] Firebase AI Logic (Gemini Developer API)
- [x] `firebase init` + `google-services` plugin
- [x] Add Firebase BoM and `firebase-ai`, remove old SDK
- [x] Initialize Firebase in `Application`
- [x] Rewrite `AiRepositoryImpl` to use Firebase AI
- [x] Update model to `gemini-2.5-flash`
- [x] Simplify API key handling (optional; Firebase handles auth)
- [x] Update `AiConfiguration` / `AiMode`
- [x] Adjust tests
- [ ] Manually test chat flow

---

## Troubleshooting

### Unresolved reference 'app'

If you see `Unresolved reference 'app'` when using `Firebase.app`:

- **Cause:** The `Firebase.app` extension property was in Firebase KTX modules, which were removed from the Firebase BOM in v34.0.0.
- **Fix:** Use `FirebaseApp.getInstance()` instead. It's the standard API and is always available in the core Firebase library.

---

## References

- [Firebase AI Logic – Get Started](https://firebase.google.com/docs/ai-logic/get-started)
- [Firebase AI Logic models](https://firebase.google.com/docs/ai-logic/models)
- [Firebase Android setup](https://firebase.google.com/docs/android/setup)
