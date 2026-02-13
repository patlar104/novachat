# NovaChat Functions

This directory contains the Firebase Functions backend for the `aiProxy` callable endpoint.

## Runtime

- Node.js 20
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
