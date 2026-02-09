# API Documentation

## Overview

NovaChat uses Firebase Cloud Functions as a proxy for AI requests:
1. **Firebase Functions Proxy** - Cloud-based AI via Firebase Functions (`aiProxy`)
2. **Google AICore** - On-device AI for offline mode (planned; currently unavailable)

## Firebase Functions Proxy

### Architecture

NovaChat uses Firebase Cloud Functions (`aiProxy`) deployed at `us-central1-novachat-13010.cloudfunctions.net/aiProxy` to handle AI requests. This provides:
- **Centralized API key management** - API keys stored securely on the server
- **Authentication** - All requests require Firebase Authentication (anonymous sign-in)
- **Usage tracking** - Server logs all AI usage for analytics

### Setup

**No user setup required** - The app automatically:
1. Signs in anonymously with Firebase Authentication
2. Calls the Firebase Function proxy for AI requests
3. Handles authentication and API key management server-side

### Usage in NovaChat

The app uses the Gemini 2.5 Flash model (via Firebase Functions proxy) for:
- Fast responses
- Cost-effective operations
- Good balance of quality and speed

### Configuration

Model parameters (configured via `AiConfiguration`):
```kotlin
temperature = 0.7f      // Creativity level (0.0-1.0)
topK = 40              // Token sampling parameter
topP = 0.95f           // Nucleus sampling parameter
maxOutputTokens = 2048 // Maximum response length
```

### Rate Limits

Rate limits vary by plan and can change. See [Google AI pricing](https://ai.google.dev/pricing) for current limits.

### Error Handling

Common errors and solutions:

| Error | Cause | Solution |
|-------|-------|----------|
| `UNAUTHENTICATED` | User not signed in | App should auto-sign-in; check Firebase Auth setup |
| `PERMISSION_DENIED` | User lacks permission | Check Firebase project permissions |
| `UNAVAILABLE` | Service unavailable | Check internet connection; retry later |
| `Network error` | No internet connection | Check connectivity |
| `INTERNAL` | Server error | Check Firebase Function logs; contact support |

## Google AICore

### Requirements

- Android 15 or higher
- Device with AICore support
- Compatible hardware (check device specs)

### Supported Devices

Not all Android 15+ devices support AICore. Check compatibility in official documentation.

### On-Device Models

AICore provides:
- Privacy (data never leaves device)
- No internet required
- Lower latency
- Free to use

### Limitations

- Smaller model size = less capable than cloud
- First download can be large
- Limited to supported devices
- Newer technology, less mature

## API Comparison

| Feature | Firebase Functions Proxy | AICore |
|---------|------------------------|--------|
| Internet Required | Yes | No |
| API Key Required | No (server-managed) | No |
| Cost | Free tier + paid | Free |
| Response Quality | Excellent | Good |
| Speed | Fast | Very Fast |
| Privacy | Cloud-based (via proxy) | On-device |
| Availability | All devices | Limited and device-dependent |

## Security Best Practices

### API Key Management

✅ **Current Architecture:**
- API keys stored securely on Firebase server (not in app)
- Firebase Functions handles API key management
- No user API keys required - app works automatically
- Anonymous authentication for user identification

❌ **Don't:**
- Hard-code keys in source code
- Store API keys in the Android app
- Make direct API calls to external services

### Data Privacy

**Online Mode (Firebase Functions Proxy):**
- Messages sent to Firebase Functions proxy
- Proxy forwards to Gemini API (Google servers)
- Subject to [Google's Privacy Policy](https://policies.google.com/privacy)
- Usage logged server-side for analytics
- Anonymous authentication used for user identification

**Offline Mode (AICore):**
- All processing on-device (when available)
- No data sent to servers
- Maximum privacy

## Code Examples

### Sending a Message (Online)

The app automatically uses Firebase Functions proxy - no API key needed:

```kotlin
val repository = AiRepositoryImpl(context)
val config = AiConfiguration(
    mode = AiMode.ONLINE,
    apiKey = null  // Not required - Firebase Functions handles it
)
val result = repository.generateResponse(
    message = "Hello, how are you?",
    configuration = config
)

result.fold(
    onSuccess = { response ->
        println("AI: $response")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
    }
)
```

### Sending a Message (Offline)

```kotlin
val repository = AiRepositoryImpl(context)
val config = AiConfiguration(
    mode = AiMode.OFFLINE,
    apiKey = null
)
val result = repository.generateResponse(
    message = "Hello, how are you?",
    configuration = config
)

result.fold(
    onSuccess = { response ->
        println("AI: $response")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
    }
)
```

### Switching AI Modes

```kotlin
// In your ViewModel
fun switchMode(newMode: AiMode) {
    viewModelScope.launch {
        preferencesRepository.saveAiMode(newMode)
    }
}
```

## Advanced Usage

### Custom Prompts

You can customize the AI behavior with system prompts:

```kotlin
val systemPrompt = """
You are a helpful assistant for Android developers.
Always provide code examples when relevant.
""".trimIndent()

val fullMessage = "$systemPrompt\n\nUser: $userMessage"
```

### Streaming Responses

Streaming responses are not currently supported via Firebase Functions proxy. The function returns complete responses.

### Error Retry Logic

Implement exponential backoff for retries:

```kotlin
suspend fun sendWithRetry(
    message: String,
    maxRetries: Int = 3
): Result<String> {
    var lastError: Exception? = null

    repeat(maxRetries) { attempt ->
        val result = sendMessageToGemini(message, apiKey)
        if (result.isSuccess) return result

        lastError = result.exceptionOrNull() as? Exception
        delay((2.0.pow(attempt) * 1000).toLong())
    }

    return Result.failure(lastError!!)
}
```

## Monitoring and Analytics

### Logging

Add logging for debugging:

```kotlin
Log.d("AiRepository", "Sending message: ${message.take(50)}...")
Log.d("AiRepository", "Response received: ${response.take(50)}...")
```

### Performance Tracking

Monitor response times:

```kotlin
val startTime = System.currentTimeMillis()
val response = sendMessage(message)
val duration = System.currentTimeMillis() - startTime
Log.d("Performance", "Response time: ${duration}ms")
```

## Resources

- [Firebase Cloud Functions Documentation](https://firebase.google.com/docs/functions)
- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [AICore Documentation](https://developer.android.com/ai/aicore)
- [Android AI Developer Guide](https://developer.android.com/ai)

## Support

For API issues:
- Firebase Functions: Check Firebase Console logs for function errors
- AICore: [Android Issue Tracker](https://issuetracker.google.com/issues?q=componentid:192708%2B)

For app-specific issues:
- Check the [GitHub Issues](https://github.com/patlar104/novachat/issues)
- Read the [Development Guide](DEVELOPMENT.md)
- See [Firebase Proxy Architecture](docs/FIREBASE_AI_MIGRATION_PLAN.md)
