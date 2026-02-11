import { HttpsError } from "firebase-functions/v2/https";

export function mapToHttpsError(error: unknown): HttpsError {
  if (error instanceof HttpsError) {
    return error;
  }

  const message =
    error instanceof Error ? error.message : "Unknown error occurred";

  if (/unauth/i.test(message)) {
    return new HttpsError(
      "unauthenticated",
      "Authentication required.",
      message
    );
  }

  if (/invalid|argument|payload|message/i.test(message)) {
    return new HttpsError("invalid-argument", "Invalid request.", message);
  }

  if (/permission/i.test(message)) {
    return new HttpsError("permission-denied", "Permission denied.", message);
  }

  if (/unavailable|timeout|network/i.test(message)) {
    return new HttpsError(
      "unavailable",
      "AI service temporarily unavailable.",
      message
    );
  }

  return new HttpsError(
    "internal",
    "Failed to generate AI response. Please try again.",
    message
  );
}
