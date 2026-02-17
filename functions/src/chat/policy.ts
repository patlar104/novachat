/**
 * Content policy: pre-checks (block patterns), post-checks (redact secrets).
 */

const BLOCK_PATTERNS = [
  /\b(api[_-]?key|apikey)\s*[:=]\s*['\"]?[a-zA-Z0-9_-]{20,}/i,
  /\b(secret|password|passwd)\s*[:=]\s*['\"]?\S+/i,
  /AIza[0-9A-Za-z_-]{35}/,
  /eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/, // JWT-like
];

const REDACT_PATTERNS = [
  /AIza[0-9A-Za-z_-]{35}/g,
  /eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/g,
  /\b(api[_-]?key|apikey)\s*[:=]\s*['\"]?[a-zA-Z0-9_-]{20,}/gi,
];

export type PolicyResult =
  | { allowed: true }
  | { allowed: false; code: string; message: string };

export function preCheckContent(messageText: string): PolicyResult {
  const trimmed = messageText.trim();
  for (const pattern of BLOCK_PATTERNS) {
    if (pattern.test(trimmed)) {
      return {
        allowed: false,
        code: "POLICY_BLOCK",
        message: "Request blocked by content policy.",
      };
    }
  }
  return { allowed: true };
}

export function postCheckRedact(text: string): string {
  let out = text;
  for (const pattern of REDACT_PATTERNS) {
    out = out.replace(pattern, "[REDACTED]");
  }
  return out;
}
