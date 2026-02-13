import { defineString } from "firebase-functions/params";

const geminiApiKeyParam = defineString("GEMINI_API_KEY");

export function getGeminiApiKey(): string {
  try {
    const paramValue = geminiApiKeyParam.value();
    if (paramValue) {
      return paramValue;
    }
  } catch {
    // Fallback below for local/spark deployments.
  }

  const envValue = process.env.GEMINI_API_KEY;
  if (!envValue) {
    throw new Error("GEMINI_API_KEY is not configured");
  }
  return envValue;
}
