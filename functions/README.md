# NovaChat Functions

This directory contains the Firebase Functions backend for the `aiProxy` callable endpoint. Full backend contracts and Path A/B behavior: **`specs/NOVACHAT_ARCHITECTURE_SPEC.md`** §3.

## Runtime

- Node.js 24 (defined by `engines` in package.json and `.nvmrc`). Use Node 24 when running the emulator or deploy. If you use Homebrew: `brew install node@24` then run the **Firebase: start emulators** task (it uses `node@24` from Homebrew); or in a terminal run `export PATH="/opt/homebrew/opt/node@24/bin:/usr/local/opt/node@24/bin:$PATH"` (Apple Silicon / Intel) before `npx firebase emulators:start`.
- TypeScript
- Firebase Functions v2

## Source layout

- `src/index.ts`: export wiring.
- `src/functions/aiProxy.ts`: callable function handler.
- `src/ai/validateRequest.ts`: payload validation.
- `src/ai/geminiClient.ts`: Gemini request execution.
- `src/ai/errors.ts`: conversion to `HttpsError`.
- `src/analytics/usageLogger.ts`: usage logging.
- `src/config/env.ts`: API key/env access.

## Required environment

- `GEMINI_API_KEY` must be available to the functions runtime.

## Local commands

```bash
npm install
npm run build
npm run lint
npm test
npm run serve
```

To (re)configure emulators from the project root use `firebase init emulators` (no trailing dot—`emulators.` is invalid).

## Callable contract summary

Input:

```ts
{
  message: string;
  modelParameters?: {
    temperature?: number;
    topK?: number;
    topP?: number;
    maxOutputTokens?: number;
  };
}
```

Output:

```ts
{
  response: string;
  model: string;
}
```

Authentication is required (`request.auth` must be present).
