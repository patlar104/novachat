# Firebase Functions - AI Proxy

This directory contains Cloud Functions for NovaChat's AI proxy service.

## Setup

1. Install dependencies:
   ```bash
   cd functions
   npm install
   ```

2. Set up your Gemini API key:
   
   **Option A: Using Firebase Secrets (requires Blaze plan):**
   ```bash
   firebase functions:secrets:set GEMINI_API_KEY
   # Then enter your API key when prompted
   ```
   
   **Option B: Set as environment variable (works on Spark plan):**
   ```bash
   export GEMINI_API_KEY="YOUR_API_KEY"
   firebase deploy --only functions
   ```
   
   **Option C: Use .env.local file (for local development):**
   ```bash
   # Create .env.local file in functions/ directory
   echo "GEMINI_API_KEY=your-key-here" > functions/.env.local
   # Then deploy (the function will read from process.env)
   ```
   
   **Important:** The function uses the new `params` package. If you're on Spark plan, 
   use Option B (environment variable). The function will automatically use it.

3. Build the TypeScript code:
   ```bash
   npm run build
   ```

4. Test locally (optional):
   ```bash
   npm run serve
   ```

5. Deploy:
   ```bash
   npm run deploy
   # or
   firebase deploy --only functions
   ```

## Function Details

### `aiProxy`

**Endpoint:** `https://us-central1-novachat-13010.cloudfunctions.net/aiProxy`

**Authentication:** Requires Firebase ID token

**Method:** Callable function (use Firebase SDK's `functions.httpsCallable()`)

**Request:**
```typescript
{
  message: string;
  modelParameters?: {
    temperature?: number;
    topK?: number;
    topP?: number;
    maxOutputTokens?: number;
  }
}
```

**Response:**
```typescript
{
  response: string;
  model: "gemini-2.5-flash";
}
```

## Environment Configuration

- **Staging:** Use a separate Firebase project or function name
- **Production:** Deploy to `novachat-13010` project

## Security

- All requests require Firebase Authentication
- API key is stored securely in Firebase config/secrets
- Usage is logged to Firestore for analytics
