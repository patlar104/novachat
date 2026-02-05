# API Documentation

## Overview

NovaChat uses two AI APIs:
1. **Google Gemini API** - Cloud-based AI for online mode
2. **Google AICore** - On-device AI for offline mode (planned; currently unavailable)

## Google Gemini API

### Setup

1. Visit [Google AI Studio](https://ai.google.dev/)
2. Sign in with your Google account  
3. Click "Get API Key"
4. Create a new project or select existing
5. Copy the generated API key

### Usage in NovaChat

The app uses the Gemini 1.5 Flash model for:
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
| `API key not valid` | Invalid or expired key | Generate new key in AI Studio |
| `Quota exceeded` | Rate limit reached | Wait or upgrade plan |
| `Network error` | No internet connection | Check connectivity |
| `Model not found` | Wrong model name | Verify model name in code |

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

| Feature | Gemini API | AICore |
|---------|-----------|--------|
| Internet Required | Yes | No |
| API Key Required | Yes | No |
| Cost | Free tier + paid | Free |
| Response Quality | Excellent | Good |
| Speed | Fast | Very Fast |
| Privacy | Cloud-based | On-device |
| Availability | All devices | Limited and device-dependent |

## Security Best Practices

### API Key Storage

✅ **Do:**
- Store API keys in DataStore (encrypted)
- Use environment variables for development
- Keep keys out of version control
- Rotate keys periodically

❌ **Don't:**
- Hard-code keys in source code
- Share keys publicly
- Commit keys to Git
- Use the same key for multiple apps

### Data Privacy

**Online Mode (Gemini):**
- Messages sent to Google servers
- Subject to [Google's Privacy Policy](https://policies.google.com/privacy)
- Data used to improve models (unless opted out)

**Offline Mode (AICore):**
- All processing on-device (when available)
- No data sent to servers
- Maximum privacy

## Code Examples

### Sending a Message (Online)

```kotlin
val repository = AiRepositoryImpl(context)
val config = AiConfiguration(
    mode = AiMode.ONLINE,
    apiKey = ApiKey.create("your-api-key-here")
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

For real-time responses, use the streaming API:

```kotlin
generativeModel.generateContentStream(message)
    .collect { chunk ->
        println(chunk.text)
    }
```

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

- [Gemini API Documentation](https://ai.google.dev/docs)
- [AICore Documentation](https://developer.android.com/ai/aicore)
- [Google AI Studio](https://ai.google.dev/)
- [Android AI Developer Guide](https://developer.android.com/ai)

## Support

For API issues:
- Gemini API: [Google AI Support](https://ai.google.dev/support)
- AICore: [Android Issue Tracker](https://issuetracker.google.com/issues?q=componentid:192708%2B)

For app-specific issues:
- Check the [GitHub Issues](https://github.com/patlar104/novachat/issues)
- Read the [Development Guide](DEVELOPMENT.md)
