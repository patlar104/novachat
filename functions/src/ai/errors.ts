import { HttpsError } from "firebase-functions/v2/https";

const UNAUTHENTICATED_PATTERN = /\bunauth(?:enticated|orized)?\b/i;
const CONFIGURATION_PATTERN = /\bGEMINI_API_KEY\b|api key is not configured/i;
const PERMISSION_DENIED_PATTERN = /\bpermission\b/i;
const UNAVAILABLE_PATTERN =
  /\b(unavailable|timeout|timed out|network|quota|rate limit|too many requests|resource exhausted)\b/i;
const INVALID_ARGUMENT_PATTERN =
  /\b(invalid argument|invalid request|bad request|malformed|payload|parameter|required|must be|expected)\b/i;

export function mapToHttpsError(error: unknown): HttpsError {
  if (error instanceof HttpsError) {
    return error;
  }

  const message =
    error instanceof Error ? error.message : "Unknown error occurred";

  if (CONFIGURATION_PATTERN.test(message)) {
    return new HttpsError(
      "internal",
      "AI service configuration error. Please contact support."
    );
  }

  if (UNAUTHENTICATED_PATTERN.test(message)) {
    return new HttpsError("unauthenticated", "Authentication required.");
  }

  if (PERMISSION_DENIED_PATTERN.test(message)) {
    return new HttpsError("permission-denied", "Permission denied.");
  }

  if (UNAVAILABLE_PATTERN.test(message)) {
    return new HttpsError("unavailable", "AI service temporarily unavailable.");
  }

  if (INVALID_ARGUMENT_PATTERN.test(message)) {
    return new HttpsError("invalid-argument", "Invalid request.");
  }

  return new HttpsError(
    "internal",
    "Failed to generate AI response. Please try again."
  );
}
