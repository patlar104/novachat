import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import { onCall } from "firebase-functions/v2/https";
import { callGemini } from "../ai/geminiClient";
import { mapToHttpsError } from "../ai/errors";
import { validateAiProxyRequest } from "../ai/validateRequest";
import { logAiUsage } from "../analytics/usageLogger";
import { getGeminiApiKey } from "../config/env";

export const aiProxy = onCall(
  {
    region: "us-central1",
    cors: true,
  },
  async (request) => {
    if (!request.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "Authentication required. Please sign in."
      );
    }

    try {
      const validatedRequest = validateAiProxyRequest(request.data);
      const apiKey = getGeminiApiKey();
      const result = await callGemini(apiKey, validatedRequest);

      await logAiUsage({
        userId: request.auth.uid,
        messageLength: validatedRequest.message.length,
        responseLength: result.response.length,
      });

      return result;
    } catch (error: unknown) {
      functions.logger.error("AI Proxy error:", error);
      throw mapToHttpsError(error);
    }
  }
);

export function initializeFunctionsAdmin(): void {
  if (!admin.apps.length) {
    admin.initializeApp();
  }
}
