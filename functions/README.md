# NovaChat Functions

This directory contains the Firebase Functions backend for the `aiProxy` callable endpoint. Full backend contracts and Path A/B behavior: **`specs/NOVACHAT_ARCHITECTURE_SPEC.md`** §3.

## Runtime

- Node.js 24 (defined by `engines` in package.json and `.nvmrc`)
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

## Dependencies

Use the package manager for all dependency changes so the lockfile stays in sync and versions resolve correctly. Avoid editing `package.json` by hand.

```bash
# Add or update a dev dependency (examples)
npm install -D eslint@^9.0.0
npm install -D some-pkg@latest

# Add or update a production dependency
npm install some-pkg@^1.0.0
```

After any dependency change, run `npm install` and then `npm run build && npm run lint && npm test`.

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
