# Screenshots

## Chat Screen

The main chat interface features:

- Clean, modern Material Design 3 interface
- User messages appear on the right in blue bubbles
- AI responses appear on the left in gray bubbles
- Smooth auto-scrolling as new messages arrive
- Loading indicator when AI is processing
- Error banners for any issues

### Empty State

When you first open the app, you'll see a welcoming empty state with:

- Friendly emoji greeting
- Instructions to start chatting
- Clean, minimal design

### Active Conversation

During a conversation:

- Messages are displayed in chronological order
- Clear visual distinction between user and AI messages
- Responsive input field at the bottom
- Send button that activates when you type

## Settings Screen

The settings interface includes:

- **AI Mode Selection**: Toggle between Online (Gemini) and Offline (On-device) modes
- **App Information**: Version details and about section
- **Theme Mode**: Light, Dark, or System default
- **Dynamic Color**: Toggle for Android 12+ dynamic theming

### Online Mode Settings

- Radio button selection for Online mode
- Changes persist immediately via DataStore

### Offline Mode Settings

- Radio button selection for Offline mode
- Information about AICore requirements
- Note about device compatibility and current unavailability

## UI Features

### Material Design 3

- Dynamic color theming (adapts to Android 12+ system theme)
- Consistent spacing and typography
- Smooth animations and transitions
- Accessible touch targets

### Responsive Design

- Adapts to different screen sizes
- Keyboard-aware layout (input field moves with keyboard)
- Portrait and landscape support

### Dark Mode Support

- Automatically follows system theme
- Optimized colors for both light and dark themes
- Comfortable viewing in any lighting condition

## Building and Testing

To see the app in action, build and run it on an Android device or emulator:

```bash
./gradlew installDebug
```

Then launch the app from your device's app drawer.

### Recommended Testing Scenarios

1. **First Launch**: See the empty state and automatic Firebase sign-in
2. **Configure Settings**: Select AI mode (no API key required)
3. **Send Messages**: Try various prompts to test AI responses via Firebase proxy
4. **Error Handling**: Test network errors and Firebase authentication errors
5. **Mode Switching**: Toggle between online and offline modes
6. **Theme Testing**: Switch device between light and dark mode

## Future Screenshots

Once you build the app, consider adding actual screenshots here showing:

- Empty state
- Active chat conversation
- Settings screen with filled values
- Error states
- Different themes (light/dark)

You can take screenshots using:

- Android Studio: Logcat window → Camera icon
- Device: Power + Volume Down buttons
- ADB: `adb exec-out screencap -p > screenshot.png`

---

_Note: This is a placeholder file. Actual screenshots will be added once the app is built and run on a device or emulator._
