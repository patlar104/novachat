# Quick Start Guide for Beginners

Welcome to NovaChat! This guide will help you get started even if you're new to Android development.

## What You Need

### Required Software

1. **Java Development Kit (JDK) 17**
   - Download from: https://adoptium.net/
   - Choose: Temurin 17 (LTS)
   - Install and verify: `java -version`

2. **Android Studio**
   - Download from: https://developer.android.com/studio
   - Latest stable version
   - Includes Android SDK automatically

### System Requirements

- **OS**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: 8 GB minimum (16 GB recommended)
- **Disk Space**: 10 GB for Android Studio + tools
- **Internet**: Required for downloading dependencies

## Step-by-Step Setup

### 1. Install Android Studio

1. Download Android Studio from the link above
2. Run the installer
3. Follow the setup wizard
4. Choose "Standard" installation
5. Wait for SDK components to download

### 2. Clone the Repository

**Option A: Using Android Studio**
1. Open Android Studio
2. Click "Get from VCS"
3. Enter: `https://github.com/patlar104/novachat.git`
4. Choose a location and click "Clone"

**Option B: Using Git Command Line**
```bash
git clone https://github.com/patlar104/novachat.git
cd novachat
```

Then open the folder in Android Studio:
- File → Open
- Navigate to the cloned folder
- Click OK

### 3. Set Up Firebase

**This is a critical step!** This app uses Firebase for its backend. You need to connect the app to your own Firebase project.

1. **Create a Firebase Project:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Click "Add project" and follow the on-screen instructions.

2. **Add an Android App to your Project:**
   - In your new project, click the Android icon (</>) to add an Android app.
   - For **Android package name**, enter `com.novachat.app`.
   - You can leave the other fields blank.
   - Click "Register app".

3. **Download `google-services.json`:**
   - After registering, you will be prompted to download a `google-services.json` file. Download it.
   - **Important:** If you miss this, go to Project Settings (⚙️) > Your apps, and you can download it from there.

4. **Add the file to your project:**
   - Move the downloaded `google-services.json` file into the `app` folder of the project (`novachat/app/`).

5. **Add SHA-1 Fingerprints (to prevent runtime errors):**
   - In the Firebase Console, go to Project Settings (⚙️) > Your apps > SHA certificate fingerprints.
   - Click "Add fingerprint".
   - In Android Studio, open the **Terminal** and run `./gradlew signingReport`.
   - Find the SHA-1 key for the `debug` variant and copy it.
   - Paste the SHA-1 key into the Firebase console and save.

### 4. Wait for Gradle Sync

When you first open the project (or after adding `google-services.json`):
1. Android Studio will show "Gradle Sync" in the bottom
2. This downloads all dependencies (may take 5-10 minutes)
3. Watch the progress bar at the bottom
4. Once done, you'll see "BUILD SUCCESSFUL"

**If Sync Fails:**
- Check your internet connection
- Try: File → Invalidate Caches → Invalidate and Restart
- Ensure JDK 17 is installed

### 5. Set Up an Android Device

**Option A: Use a Real Device (Recommended)**
1. Enable Developer Options on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect phone to computer with USB cable
4. Tap "Allow" on the phone when prompted

**Option B: Use an Emulator**
1. In Android Studio: Tools → Device Manager
2. Click "Create Device"
3. Choose a device (e.g., Pixel 6)
4. Download a system image (API 35 / Android 16)
5. Click "Finish" and start the emulator

### 6. Run the App

1. In Android Studio toolbar, select your device
2. Click the green "Run" button (▶️)
3. Wait for the app to build (1-2 minutes first time)
4. App will install and launch on your device

### 7. Configure the App

1. When app opens, it will automatically sign in anonymously with Firebase
2. Tap the ⚙️ (Settings) icon
3. Choose "Online (Gemini)" mode (default)
4. Tap the back arrow to return to chat

### 8. Start Chatting!

1. Type a message in the input field
2. Tap the send button (📤)
3. Wait for the AI to respond
4. Continue the conversation!

## Common Beginner Issues

### "App Crashes on Launch" or "SecurityException"

**Problem**: The app can't connect to Firebase.
**Solution**:
1. Ensure you have correctly completed **Step 3: Set Up Firebase**.
2. Make sure `google-services.json` is in the `novachat/app/` directory.
3. Check that you have added the SHA-1 fingerprint to your Firebase project settings.
4. Try: Build → Clean Project → Rebuild

### "Gradle Sync Failed"

**Problem**: Dependencies couldn't download
**Solution**:
1. Check internet connection
2. Wait a few minutes and retry
3. File → Invalidate Caches → Restart
4. Check if firewall blocks Android Studio

### "SDK Not Found"

**Problem**: Android SDK not installed
**Solution**:
1. Tools → SDK Manager
2. Check "Android 16 (API 35)" and "Android 17 (API 36)"
3. Click "Apply" to install
4. Restart Android Studio

### "Device Not Detected"

**Problem**: Phone not showing in device list
**Solution**:
1. Check USB cable is connected
2. Try a different USB port
3. Enable USB Debugging (see step 4 above)
4. Restart phone and computer
5. Install phone manufacturer's USB drivers

### "Build Failed with Errors"

**Problem**: Code won't compile
**Solution**:
1. Build → Clean Project
2. Build → Rebuild Project
3. Check the "Build" tab for specific errors
4. File → Invalidate Caches → Restart

## Learning Resources

### Android Basics
- [Android Basics Course](https://developer.android.com/courses/android-basics-compose/course)
- [Kotlin for Beginners](https://kotlinlang.org/docs/getting-started.html)

### This Project
- [README.md](README.md) - Project overview
- [DEVELOPMENT.md](DEVELOPMENT.md) - Development guide
- [API.md](API.md) - API documentation

### Getting Help
1. Check existing [GitHub Issues](https://github.com/patlar104/novachat/issues)
2. Create a new issue with:
   - What you tried to do
   - What happened instead
   - Error messages (if any)
   - Your Android Studio version

## Next Steps

Once you have the app running:

1. **Experiment**: Try different prompts and see how the AI responds
2. **Explore the Code**: Look at the Kotlin files to understand how it works
3. **Make Changes**: Try changing the UI colors or text
4. **Learn More**: Follow the development guide to add features

## Tips for Success

✅ **Do:**
- Take your time with each step
- Read error messages carefully
- Search for errors online (Stack Overflow is your friend!)
- Experiment and learn from mistakes
- Ask for help when stuck

❌ **Don't:**
- Skip steps in the guide
- Panic when things don't work first try
- Share your API key publicly
- Make many changes at once (change one thing at a time)

## Congratulations! 🎉

You've successfully set up your first Android AI app! Keep learning and building awesome things!

Remember: Every expert was once a beginner. Take it one step at a time, and don't be afraid to make mistakes—that's how you learn!

---

**Need More Help?**
- Read the [README.md](README.md)
- Check [Common Issues](#common-beginner-issues)
- Ask on [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- Open a [GitHub Issue](https://github.com/patlar104/novachat/issues/new)
