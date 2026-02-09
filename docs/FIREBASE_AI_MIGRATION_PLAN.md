# Firebase Proxy Architecture

NovaChat uses Firebase Cloud Functions as a proxy for AI requests, providing centralized API key management, authentication, and usage tracking.

## Current Architecture

**Firebase Function:** `aiProxy` deployed at `us-central1-novachat-13010.cloudfunctions.net/aiProxy`

**Authentication:** Anonymous Firebase Authentication (auto-sign-in on app startup)

**Dependencies Required:**
- `firebase-functions` - Firebase Functions SDK (KTX functionality now in main module, BOM v34.0.0+)
- `firebase-auth` - Firebase Authentication SDK (KTX functionality now in main module, BOM v34.0.0+)
- `kotlinx-coroutines-play-services` - Coroutines support for Firebase Tasks

**API Usage:**
- Use `FirebaseFunctions.getInstance("region")` instead of deprecated `Firebase.functions()` extension
  - Example: `FirebaseFunctions.getInstance("us-central1")`
- Use `FirebaseAuth.getInstance()` instead of deprecated `Firebase.auth` extension
  - Example: `FirebaseAuth.getInstance()`

## Key Files

- `NovaChatApplication.kt` - Initializes Firebase Auth and signs in anonymously
- `AiRepositoryImpl.kt` - Calls Firebase Function `aiProxy` instead of direct API calls
- `functions/src/index.ts` - Cloud Function implementation (TypeScript)

## Maintenance Rules

### Backend Agent Responsibilities

When modifying AI integration:
- Always use Firebase Functions callable (`functions.getHttpsCallable("aiProxy")`)
- Never call Gemini API directly from Android app
- Ensure anonymous authentication is initialized before making requests
- Handle FirebaseFunctionsException with proper error codes (UNAUTHENTICATED, PERMISSION_DENIED, etc.)
- Validate authentication state before calling functions

### Function Deployment

- Function code lives in `functions/src/index.ts`
- Deploy with: `firebase deploy --only functions`
- API key stored in Firebase config/secrets (GEMINI_API_KEY parameter)
- Function requires Firebase ID token authentication

### Authentication Flow

- App automatically signs in anonymously in `NovaChatApplication.onCreate()`
- Sign-in happens asynchronously in applicationScope
- Repository checks `auth.currentUser` before making function calls
- Errors handled gracefully if sign-in fails

## Checklist

- [x] Firebase project configured
- [x] Cloud Function deployed
- [x] Anonymous Authentication enabled in Firebase Console
- [x] Android app uses Firebase Functions proxy
- [x] Error handling for all Firebase Functions exceptions
