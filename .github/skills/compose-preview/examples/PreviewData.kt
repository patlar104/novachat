package com.novachat.app.ui.previews

import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageId
import com.novachat.app.domain.model.MessageSender

/**
 * Test data builders for preview rendering.
 *
 * These functions create realistic test data for use in composable previews
 * without needing to mock or access real data sources.
 *
 * Usage:
 * ```
 * val testMessage = previewUserMessage("Hello world")
 * val conversation = listOf(
 *     previewUserMessage("Where is Rome?"),
 *     previewAiMessage("Rome is the capital of Italy...")
 * )
 * ```
 *
 * @see Message
 * @see MessageSender
 */

// ============================================================
// MESSAGE BUILDERS
// ============================================================

/**
 * Creates a test user message for preview rendering.
 *
 * @param content The message text
 * @param id Unique message ID (auto-generated if omitted)
 * @param timestamp Message timestamp in milliseconds (defaults to current time)
 * @return A Message with user sender
 */
fun previewUserMessage(
    content: String,
    id: String = "user-${System.nanoTime()}",
    timestamp: Long = System.currentTimeMillis()
): Message = Message(
    id = MessageId(id),
    content = content,
    sender = MessageSender.USER,
    timestamp = timestamp
)

/**
 * Creates a test AI assistant message for preview rendering.
 *
 * @param content The message text
 * @param id Unique message ID (auto-generated if omitted)
 * @param timestamp Message timestamp in milliseconds (defaults to current time)
 * @return A Message with assistant sender
 */
fun previewAiMessage(
    content: String,
    id: String = "ai-${System.nanoTime()}",
    timestamp: Long = System.currentTimeMillis()
): Message = Message(
    id = MessageId(id),
    content = content,
    sender = MessageSender.ASSISTANT,
    timestamp = timestamp
)

// ============================================================
// TEST CONVERSATION DATASETS
// ============================================================

/**
 * Short test conversation (6 messages total, 3 exchanges).
 *
 * Use for: Quick previews, component testing, empty state transitions
 */
val shortTestMessages = listOf(
    previewUserMessage("Hello! Who are you?"),
    previewAiMessage("I'm NovaChat, an AI assistant. How can I help?"),
    previewUserMessage("Can you tell me about Kotlin?"),
    previewAiMessage("Kotlin is a modern programming language for Android, more concise than Java."),
    previewUserMessage("Thanks!"),
    previewAiMessage("You're welcome! Any other questions?")
)

/**
 * Standard test conversation (12 messages total, 6 exchanges).
 *
 * Use for: Full-screen previews, scrolling tests, typical chat scenarios
 */
val testMessages = listOf(
    previewUserMessage("Hello! Who are you?"),
    previewAiMessage("I'm NovaChat, an AI assistant built with Jetpack Compose. How can I help?"),
    previewUserMessage("Can you tell me a joke?"),
    previewAiMessage("Why don't developers go outside? Because Java has no sun! ☀️"),
    previewUserMessage("That's funny! Tell me another one"),
    previewAiMessage("What do you call a programmer from Finland? Nerdic! 😄"),
    previewUserMessage("Great! Now tell me about yourself"),
    previewAiMessage("I'm an AI trained to have helpful conversations. I can answer questions, discuss topics, and have fun!"),
    previewUserMessage("What's your favorite programming language?"),
    previewAiMessage("I would have to say Kotlin! It's designed to be safe, concise, and interoperable with Java."),
    previewUserMessage("Interesting choice!"),
    previewAiMessage("It really is fantastic for modern Android development. Thanks for chatting with me!")
)

/**
 * Long test conversation (40+ messages).
 *
 * Use for: Scroll performance testing, large list handling, memory tests
 */
val longTestMessages = (1..20).flatMap { i ->
    listOf(
        previewUserMessage("Question $i: Can you explain concept $i?"),
        previewAiMessage("Of course! Concept $i is an important topic in Android development. It relates to...")
    )
}

/**
 * Test conversation with long messages.
 *
 * Use for: Text wrapping tests, display of multi-line messages
 */
val longMessageTestMessages = listOf(
    previewUserMessage(
        "I have a question about the project structure. Can you explain how the " +
        "data layer, domain layer, and presentation layer interact with each other? " +
        "I'm particularly interested in how repositories bridge the gap between the " +
        "data and domain layers."
    ),
    previewAiMessage(
        "Great question! The architecture is structured as follows:\n" +
        "\n" +
        "1. **Data Layer**: Handles all data sources (APIs, databases, preferences)\n" +
        "2. **Domain Layer**: Contains business logic via use cases, is independent of Android\n" +
        "3. **Presentation Layer**: ViewModels and UI, depends on domain layer only\n" +
        "\n" +
        "Repositories in the data layer implement interfaces defined in the domain layer. " +
        "This creates an abstraction that allows use cases to remain Android-agnostic and testable."
    ),
    previewUserMessage(
        "That makes sense. So the dependency flow is: " +
        "Data → Domain ← Presentation, and nothing in Domain imports from Data?"
    ),
    previewAiMessage(
        "Exactly right! That's the key principle. The domain layer never imports from data. " +
        "Instead, the data layer provides implementations of domain interfaces. This is known " +
        "as the Dependency Inversion Principle and enables loose coupling and easier testing."
    )
)

/**
 * Test conversation with emoji and special characters.
 *
 * Use for: Text rendering, emoji support, internationalization tests
 */
val emojiTestMessages = listOf(
    previewUserMessage("How are you today? 😊"),
    previewAiMessage("I'm doing great! 🚀 Thanks for asking!"),
    previewUserMessage("That's awesome! 🎉"),
    previewAiMessage("Yes! 💯 What would you like to talk about?"),
    previewUserMessage("Tell me about Compose! 🎨💻"),
    previewAiMessage(
        "Jetpack Compose is amazing! ✨\n" +
        "✅ Declarative UI\n" +
        "✅ Hot reloading\n" +
        "✅ Composable components\n" +
        "✅ Modern tooling\n" +
        "\nIt's the future of Android UI! 🎯"
    )
)

/**
 * Test conversation with very short messages.
 *
 * Use for: Compact layouts, spacing tests
 */
val shortMessageTestMessages = listOf(
    previewUserMessage("Hi"),
    previewAiMessage("Hello!"),
    previewUserMessage("How are you?"),
    previewAiMessage("Good!"),
    previewUserMessage("Great!"),
    previewAiMessage("Same here!")
)

/**
 * Test conversation with mixed message lengths.
 *
 * Use for: Realistic chat scenarios with varying content
 */
val mixedLengthMessages = listOf(
    previewUserMessage("Hi"),
    previewAiMessage("Hello! How can I assist you today?"),
    previewUserMessage("Tell me about Kotlin"),
    previewAiMessage("Kotlin"),
    previewUserMessage("No, elaborate more"),
    previewAiMessage(
        "Kotlin is a statically typed programming language that runs on the JVM. " +
        "It's designed to be more concise and safer than Java while maintaining " +
        "full interoperability. Key features include:\n" +
        "• Null safety\n" +
        "• Extension functions\n" +
        "• Higher-order functions\n" +
        "• Coroutines support"
    ),
    previewUserMessage("Cool!"),
    previewAiMessage("It really is! Great for Android development.")
)

// ============================================================
// ERROR MESSAGE EXAMPLES
// ============================================================

/**
 * Common error messages for testing error states.
 */
object PreviewErrorMessages {
    const val NETWORK_ERROR = "Network connection failed. Please check your internet."
    const val API_KEY_INVALID = "Invalid API key. Please check your configuration."
    const val API_KEY_MISSING = "API key is required for online mode."
    const val TIMEOUT = "Request timed out. Please try again."
    const val OFFLINE_MODE_UNAVAILABLE = "Offline mode (AICore) is not yet available."
    const val SEND_FAILED = "Failed to send message. Tap to retry."
    const val LOAD_FAILED = "Failed to load messages. Please check your connection."
    const val CONFIGURATION_FAILED = "Failed to load settings. Please try again."
    const val UNKNOWN_ERROR = "An unexpected error occurred. Please try again."
}

// ============================================================
// STATIC DATASET SELECTIONS
// ============================================================

/**
 * Get a test message by index.
 *
 * @param index Index in the test messages
 * @return The test message at that index
 */
fun getTestMessage(index: Int): Message? {
    return testMessages.getOrNull(index)
}

/**
 * Get the first user message from test conversations.
 */
val firstUserMessage: Message = previewUserMessage("Hello! Who are you?")

/**
 * Get the first AI message from test conversations.
 */
val firstAiMessage: Message = previewAiMessage("I'm NovaChat, an AI assistant. How can I help?")

/**
 * Create a custom conversation from component messages.
 *
 * Example:
 * ```
 * val custom = conversationFrom(
 *     previewUserMessage("Hello"),
 *     previewAiMessage("Hi there!")
 * )
 * ```
 */
fun conversationFrom(vararg messages: Message): List<Message> = messages.toList()
