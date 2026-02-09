import * as functions from "firebase-functions/v2";
import {defineString} from "firebase-functions/params";
import * as admin from "firebase-admin";

// Initialize Firebase Admin
admin.initializeApp();

// Define environment parameter for Gemini API key
// Uses the new params system (recommended for production)
// For Spark plan, set GEMINI_API_KEY as environment variable before deployment
const geminiApiKey = defineString("GEMINI_API_KEY");

/**
 * AI Proxy Cloud Function
 * 
 * This function acts as a proxy between the Android app and Google's Gemini API.
 * It handles authentication, rate limiting, and API key management.
 * 
 * Base URL: https://us-central1-novachat-13010.cloudfunctions.net/aiProxy
 * 
 * Authentication: Requires Firebase ID token in Authorization header
 * Method: POST
 * 
 * Request body:
 * {
 *   "message": "User's message",
 *   "modelParameters": {
 *     "temperature": 0.7,
 *     "topK": 40,
 *     "topP": 0.95,
 *     "maxOutputTokens": 2048
 *   }
 * }
 */
export const aiProxy = functions.https.onCall(
  {
    region: "us-central1",
    cors: true,
  },
  async (request) => {
    // Verify authentication
    const auth = request.auth;
    if (!auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "Authentication required. Please sign in."
      );
    }

    const {message, modelParameters} = request.data;

    // Validate input
    if (!message || typeof message !== "string" || message.trim().length === 0) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Message is required and must be a non-empty string"
      );
    }

    // Get API key from environment parameter (new params system)
    // Falls back to process.env for Spark plan compatibility
    let apiKey: string;
    try {
      apiKey = geminiApiKey.value();
    } catch {
      // Fallback to process.env if params not set (for Spark plan)
      apiKey = process.env.GEMINI_API_KEY || "";
    }
    
    if (!apiKey) {
      throw new functions.https.HttpsError(
        "internal",
        "AI service configuration error. GEMINI_API_KEY not set. Please contact support."
      );
    }

    try {
      // Call Gemini API
      const geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
      
      const requestBody = {
        contents: [{
          parts: [{
            text: message,
          }],
        }],
        generationConfig: {
          temperature: modelParameters?.temperature ?? 0.7,
          topK: modelParameters?.topK ?? 40,
          topP: modelParameters?.topP ?? 0.95,
          maxOutputTokens: modelParameters?.maxOutputTokens ?? 2048,
        },
      };

      const response = await fetch(`${geminiUrl}?key=${apiKey}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          `Gemini API error: ${response.status} ${response.statusText}. ${JSON.stringify(errorData)}`
        );
      }

      const data = await response.json();
      const responseText = data.candidates?.[0]?.content?.parts?.[0]?.text;

      if (!responseText) {
        throw new Error("Empty response from AI service");
      }

      // Log usage (optional - for analytics)
      await admin.firestore().collection("ai_usage").add({
        userId: auth.uid,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        messageLength: message.length,
        responseLength: responseText.length,
      }).catch(() => {
        // Ignore logging errors
      });

      return {
        response: responseText,
        model: "gemini-2.5-flash",
      };
    } catch (error: unknown) {
      functions.logger.error("AI Proxy error:", error);
      
      // Return user-friendly error
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      
      const errorMessage = error instanceof Error ? error.message : "Unknown error occurred";
      throw new functions.https.HttpsError(
        "internal",
        "Failed to generate AI response. Please try again.",
        errorMessage
      );
    }
  }
);
