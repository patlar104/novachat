/**
 * Call Gemini with 8s timeout and retries (1s, 4s, 16s + jitter).
 */

import { getGeminiApiKey } from "../config/env";
import { postCheckRedact } from "./policy";

const GEMINI_MODEL = "gemini-2.5-flash";
const TIMEOUT_MS = 8000;
const RETRY_DELAYS_MS = [1000, 4000, 16000];
const JITTER_FRACTION = 0.3;

function jitter(ms: number): number {
  const j = 1 + (Math.random() * 2 - 1) * JITTER_FRACTION;
  return Math.round(ms * j);
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export interface WorkerGeminiOptions {
  messageText: string;
  maxOutputTokens: number;
  degraded?: boolean;
}

export async function callGeminiWithTimeout(
  options: WorkerGeminiOptions
): Promise<{ response: string; model: string; tokenEstimate: number }> {
  const apiKey = getGeminiApiKey();
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

  const body = JSON.stringify({
    contents: [{ parts: [{ text: options.messageText }] }],
    generationConfig: {
      temperature: 0.7,
      topK: 40,
      topP: 0.95,
      maxOutputTokens: options.maxOutputTokens,
    },
  });

  let lastError: Error | null = null;
  for (let attempt = 0; attempt <= RETRY_DELAYS_MS.length; attempt++) {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS);
      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": apiKey,
        },
        body,
        signal: controller.signal,
      });
      clearTimeout(timeoutId);

      if (!response.ok) {
        const text = await response.text();
        throw new Error(`Gemini API ${response.status}: ${text}`);
      }

      interface CandidatePart {
        text?: string;
      }
      interface Candidate {
        content?: { parts?: CandidatePart[] };
      }
      interface ApiResponse {
        candidates?: Candidate[];
      }
      const data = (await response.json()) as ApiResponse;
      const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text ?? "";
      if (!rawText) {
        throw new Error("Empty response from AI service");
      }
      const responseText = postCheckRedact(rawText);
      const tokenEstimate = Math.ceil(
        (options.messageText.length + responseText.length) / 4
      );
      return {
        response: responseText,
        model: GEMINI_MODEL,
        tokenEstimate,
      };
    } catch (err) {
      lastError = err instanceof Error ? err : new Error(String(err));
      if (attempt < RETRY_DELAYS_MS.length) {
        await sleep(jitter(RETRY_DELAYS_MS[attempt]));
      }
    }
  }
  throw lastError ?? new Error("Gemini call failed");
}
