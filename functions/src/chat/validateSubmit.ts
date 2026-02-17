/**
 * Validate POST /v1/chat/submit request body.
 */

import { HttpsError } from "firebase-functions/v2/https";
import type { ChatSubmitRequest, ModelProfile } from "./types";
import { MAX_MESSAGE_LENGTH } from "./types";

const MODEL_PROFILES: ModelProfile[] = ["standard", "cheap"];

export function validateSubmitBody(data: unknown): ChatSubmitRequest {
  if (typeof data !== "object" || data === null) {
    throw new HttpsError(
      "invalid-argument",
      "Request body must be a JSON object."
    );
  }

  const body = data as Record<string, unknown>;

  const requestId = body.requestId;
  if (typeof requestId !== "string" || requestId.length === 0) {
    throw new HttpsError(
      "invalid-argument",
      "requestId is required and must be a non-empty string."
    );
  }

  const conversationId = body.conversationId;
  if (typeof conversationId !== "string" || conversationId.length === 0) {
    throw new HttpsError("invalid-argument", "conversationId is required.");
  }

  const messageId = body.messageId;
  if (typeof messageId !== "string" || messageId.length === 0) {
    throw new HttpsError("invalid-argument", "messageId is required.");
  }

  const messageText = body.messageText;
  if (typeof messageText !== "string") {
    throw new HttpsError(
      "invalid-argument",
      "messageText is required and must be a string."
    );
  }
  if (messageText.length > MAX_MESSAGE_LENGTH) {
    throw new HttpsError(
      "invalid-argument",
      `messageText must be at most ${MAX_MESSAGE_LENGTH} characters.`
    );
  }
  if (messageText.trim().length === 0) {
    throw new HttpsError("invalid-argument", "messageText cannot be blank.");
  }

  const modelProfile = body.modelProfile;
  if (
    typeof modelProfile !== "string" ||
    !MODEL_PROFILES.includes(modelProfile as ModelProfile)
  ) {
    throw new HttpsError(
      "invalid-argument",
      "modelProfile must be one of: standard, cheap."
    );
  }

  const clientTsMs = body.clientTsMs;
  if (typeof clientTsMs !== "number" || !Number.isFinite(clientTsMs)) {
    throw new HttpsError(
      "invalid-argument",
      "clientTsMs is required and must be a number."
    );
  }

  const appInstanceId = body.appInstanceId;
  const appInstanceIdVal =
    appInstanceId === undefined || appInstanceId === null
      ? null
      : typeof appInstanceId === "string"
        ? appInstanceId
        : null;

  return {
    requestId,
    conversationId,
    messageId,
    messageText: messageText.trim(),
    modelProfile: modelProfile as ModelProfile,
    clientTsMs,
    appInstanceId: appInstanceIdVal,
  };
}
