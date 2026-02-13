import { HttpsError } from "firebase-functions/v2/https";
import { AiProxyValidatedRequest } from "./types";

const DEFAULT_PARAMETERS = {
  temperature: 0.7,
  topK: 40,
  topP: 0.95,
  maxOutputTokens: 2048,
};

function asFiniteNumber(value: unknown, fallback: number): number {
  return typeof value === "number" && Number.isFinite(value) ? value : fallback;
}

export function validateAiProxyRequest(data: unknown): AiProxyValidatedRequest {
  if (typeof data !== "object" || data === null) {
    throw new HttpsError(
      "invalid-argument",
      "Request payload must be an object"
    );
  }

  const payload = data as { message?: unknown; modelParameters?: unknown };

  if (
    typeof payload.message !== "string" ||
    payload.message.trim().length === 0
  ) {
    throw new HttpsError(
      "invalid-argument",
      "Message is required and must be a non-empty string"
    );
  }

  const rawParams =
    typeof payload.modelParameters === "object" &&
    payload.modelParameters !== null
      ? (payload.modelParameters as Record<string, unknown>)
      : {};

  return {
    message: payload.message.trim(),
    modelParameters: {
      temperature: asFiniteNumber(
        rawParams.temperature,
        DEFAULT_PARAMETERS.temperature
      ),
      topK: asFiniteNumber(rawParams.topK, DEFAULT_PARAMETERS.topK),
      topP: asFiniteNumber(rawParams.topP, DEFAULT_PARAMETERS.topP),
      maxOutputTokens: asFiniteNumber(
        rawParams.maxOutputTokens,
        DEFAULT_PARAMETERS.maxOutputTokens
      ),
    },
  };
}
