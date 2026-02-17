# Cursor Cloud Runtime

Secrets for cloud agents are **not** stored in this repo. Add them in Cursor:

- **Cursor Desktop:** Settings (Cmd/Ctrl + ,) → **Cloud Agents** → **Secrets**
- **Web:** [Cursor Dashboard](https://cursor.com/dashboard?tab=cloud-agents) → **Cloud Agents** → **Secrets**

## Required secret

| Name             | Description                                                     |
| ---------------- | --------------------------------------------------------------- |
| `GEMINI_API_KEY` | Gemini API key for the functions AI proxy (Firebase/emulators). |

Add `GEMINI_API_KEY` as a key-value pair in the Secrets tab; Cursor injects it as an environment variable in the cloud agent.
