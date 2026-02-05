---
name: Material Design 3 with Jetpack Compose
description: Complete Material 3 Compose patterns for NovaChat (NO placeholders, NO XML)
category: ui
applies_to:
  - "**/ui/**/*.kt"
  - "**/ui/theme/*.kt"
protocol_compliance: true
note: All examples are COMPLETE Jetpack Compose implementations - following DEVELOPMENT_PROTOCOL.md
---

# Material Design 3 with Jetpack Compose Skill

This skill provides **COMPLETE** Material Design 3 Compose patterns. All code examples are fully implemented with no placeholders.

> **⚠️ PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - ✅ Complete @Composable functions (no `// ... UI code` placeholders)
> - ✅ All imports explicitly shown
> - ✅ Material 3 components only (no Material 2)
> - ✅ Theme integration shown
> - ✅ Accessibility semantics included

## Multi-Agent Coordination

### When the UI Agent Should Use Tools

**Use tools immediately for:**
- Reading existing Composable files → `read_file`
- Creating new screen Composables → `create_file`
- Modifying Composable implementations → `replace_string_in_file`
- Searching for Material 3 patterns → `grep_search` or `semantic_search`
- Validating syntax and imports → `run_in_terminal` for build checks

**Do NOT describe; DO implement:**
- Don't say "create a screen Composable"; create it using `create_file`
- Don't say "update the theme colors"; update using `replace_string_in_file`
- Don't say "add Material 3 buttons"; add them using `replace_string_in_file`

### When to Hand Off to Other Agents

**Hand off to Backend Agent if:**
- ViewModel creation/modification is needed
- State management implementation needed
- Use case or repository integration needed
- Event handling logic needs implementation
- → **Action**: Provide Composable signature showing expected state/events

**Hand off to Preview Agent if:**
- @Preview annotations need creation
- Preview data providers need creation
- Preview state variants need setup
- → **Action**: Create Composable, then hand off for preview coverage

**Hand off to Testing Agent if:**
- UI behavior tests need creation
- Interaction testing needed
- Accessibility testing needed
- → **Action**: Implement Composable, then hand off for test coverage

**Hand off to Build Agent if:**
- Compose or Gradle dependencies missing
- Build configuration issues arise
- → **Action**: Report specific build/dependency issues

### UI Task Assessment

**Determine scope before acting:**

1. **Is this a UI task?**
   - Creating Composable screens → YES, use UI Agent tools
   - Modifying Material 3 components → YES, use UI Agent tools
   - Adjusting layout/spacing → YES, use UI Agent tools
   - Creating ViewModels → NO, hand off to Backend Agent
   - Creating tests → NO, hand off to Testing Agent
   - Creating previews → Maybe, see below

2. **Do I have all context needed?**
   - What state should this screen display? → Check UiState definition
   - What events should it emit? → Check UiEvent sealed interface
   - What Material 3 components apply? → Review patterns in this skill

3. **Is this within UI Agent scope?**
   - Creating screen Composables → YES ✓
   - Implementing Material 3 design → YES ✓
   - Building layouts and navigation → YES ✓
   - Creating ViewModels → NO, hand off to Backend Agent
   - Setting up state management → NO, hand off to Backend Agent
   - Writing tests → NO, hand off to Testing Agent

## Theme Setup (Complete)

### Color.kt - Material 3 Color System

```kotlin
package com.novachat.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors
val md_theme_light_primary = Color(0xFF006B54)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFF71F9CF)
val md_theme_light_onPrimaryContainer = Color(0xFF002117)

val md_theme_light_secondary = Color(0xFF4B635B)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCDE8DD)
val md_theme_light_onSecondaryContainer = Color(0xFF082019)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = Color(0xFFFBFDF9)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF9)
val md_theme_light_onSurface = Color(0xFF191C1A)

// Dark theme colors
val md_theme_dark_primary = Color(0xFF52DDB4)
val md_theme_dark_onPrimary = Color(0xFF00382A)
val md_theme_dark_primaryContainer = Color(0xFF00513E)
val md_theme_dark_onPrimaryContainer = Color(0xFF71F9CF)

val md_theme_dark_secondary = Color(0xFFB1CCC1)
val md_theme_dark_onSecondary = Color(0xFF1D352D)
val md_theme_dark_secondaryContainer = Color(0xFF344C44)
val md_theme_dark_onSecondaryContainer = Color(0xFFCDE8DD)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
```

### Theme.kt - Complete Theme Configuration

```kotlin
package com.novachat.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface
)

@Composable
fun NovaChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Type.kt - Complete Typography

```kotlin
package com.novachat.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

## Material 3 Components (Complete Examples)

### Buttons

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ButtonExamples() {
    // Filled button (primary action)
    Button(onClick = { /* Handle click */ }) {
        Text("Send Message")
    }
    
    // Filled tonal button
    FilledTonalButton(onClick = { /* Handle click */ }) {
        Text("Save")
    }
    
    // Outlined button (secondary action)
    OutlinedButton(onClick = { /* Handle click */ }) {
        Text("Cancel")
    }
    
    // Text button (tertiary action)
    TextButton(onClick = { /* Handle click */ }) {
        Text("Learn More")
    }
    
    // Elevated button
    ElevatedButton(onClick = { /* Handle click */ }) {
        Text("Elevated")
    }
}
```

### Text Fields

```kotlin
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TextFieldExamples() {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    // Filled text field
    TextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = "Email icon")
        },
        singleLine = true
    )
    
    // Outlined text field
    OutlinedTextField(
        value = message,
        onValueChange = { message = it },
        label = { Text("Message") },
        supportingText = { Text("Enter your message here") },
        minLines = 3,
        maxLines = 5
    )
}
```

### Cards

```kotlin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardExamples() {
    // Filled card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Card Title",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Card content goes here",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    
    // Elevated card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Text(
            text = "Elevated Card",
            modifier = Modifier.padding(16.dp)
        )
    }
    
    // Outlined card
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Outlined Card",
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

### Top App Bar

```kotlin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarExample(
    onNavigationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = { Text("NovaChat") },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Open navigation drawer"
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Open settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
```

### Dialogs

```kotlin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AlertDialogExample(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### Lists with LazyColumn

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Message(val id: Int, val content: String, val isFromUser: Boolean)

@Composable
fun MessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = messages,
            key = { message -> message.id }
        ) { message ->
            MessageItem(message = message)
        }
    }
}

@Composable
private fun MessageItem(message: Message) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message.content,
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

## Accessibility (Complete)

### Semantics for Screen Readers

```kotlin
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics {
            contentDescription = if (enabled) {
                "Send message button"
            } else {
                "Send message button, disabled"
            }
        }
    ) {
        Icon(
            Icons.Default.Send,
            contentDescription = null // Handled by button semantics
        )
    }
}
```

### Minimum Touch Targets

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccessibleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp) // Minimum touch target
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
```

## Layout Best Practices

### ConstraintLayout in Compose

```kotlin
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConstraintLayoutExample() {
    var text by remember { mutableStateOf("") }
    
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (titleRef, inputRef, buttonRef) = createRefs()
        
        Text(
            text = "Enter Message",
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
            }
        )
        
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.constrainAs(inputRef) {
                top.linkTo(titleRef.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            }
        )
        
        Button(
            onClick = { /* Send */ },
            modifier = Modifier.constrainAs(buttonRef) {
                top.linkTo(inputRef.bottom, margin = 8.dp)
                end.linkTo(parent.end, margin = 16.dp)
            }
        ) {
            Text("Send")
        }
    }
}
```

## Protocol Compliance Checklist

Before submitting Compose UI code, verify:

- [ ] **Complete @Composable functions** - No `// ... UI code` placeholders
- [ ] **All imports included** - Every Compose import explicitly listed
- [ ] **Material 3 components** - Using M3, not M2
- [ ] **Theme colors used** - From MaterialTheme.colorScheme, not hardcoded
- [ ] **Typography used** - From MaterialTheme.typography
- [ ] **Accessibility** - ContentDescription or semantics provided
- [ ] **Preview functions** - @Preview annotations for key Composables
- [ ] **State hoisting** - Stateless Composables with parameters
- [ ] **Modifiers** - Proper Modifier chaining and sizing

**Remember: DEVELOPMENT_PROTOCOL.md prohibits placeholder code in ALL files, including UI!**

---

**End of Material Design 3 with Jetpack Compose Skill**
