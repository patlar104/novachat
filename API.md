# API and Contract Notes

## Android-side contracts

## AI configuration

- `AiConfiguration` contains:
  - `mode` (`ONLINE` or `OFFLINE`)
  - `modelParameters` (`temperature`, `topK`, `topP`, `maxOutputTokens`)

## Offline capability contract

- `OfflineCapability.Checking`
- `OfflineCapability.Available`
- `OfflineCapability.Unavailable(reason)`

This allows OFFLINE to remain visible in UI while presenting explicit availability state.

## Network transport contracts (`core-network`)

### Request

`AiProxyRequest`

- `message: String`
- `temperature: Float`
- `topK: Int`
- `topP: Float`
- `maxOutputTokens: Int`

### Response

`AiProxyResponse`

- `response: String`
- `model: String?`

Transport is executed through Firebase callable function `aiProxy`.

## Cloud Functions callable contract

### Callable name

- `aiProxy`

### Validated input shape

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

### Output shape

```ts
{
  response: string;
  model: string;
}
```

## Error behavior

Functions error mapping normalizes unknown failures into `HttpsError` categories:

- `invalid-argument`
- `unauthenticated`
- `permission-denied`
- `unavailable`
- `internal`

Android-side handling should treat these as user-facing recoverable/terminal states based on context.
