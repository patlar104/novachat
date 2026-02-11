import { GeminiResponse } from "./types";

interface GeminiCandidatePart {
  text?: string;
}

interface GeminiCandidate {
  content?: {
    parts?: GeminiCandidatePart[];
  };
}

interface GeminiApiResponse {
  candidates?: GeminiCandidate[];
}

interface GeminiRequest {
  message: string;
  modelParameters: {
    temperature: number;
    topK: number;
    topP: number;
    maxOutputTokens: number;
  };
}

const GEMINI_MODEL = "gemini-2.5-flash";

export async function callGemini(
  apiKey: string,
  request: GeminiRequest
): Promise<GeminiResponse> {
  const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

  const response = await fetch(`${geminiUrl}?key=${apiKey}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      contents: [
        {
          parts: [
            {
              text: request.message,
            },
          ],
        },
      ],
      generationConfig: {
        temperature: request.modelParameters.temperature,
        topK: request.modelParameters.topK,
        topP: request.modelParameters.topP,
        maxOutputTokens: request.modelParameters.maxOutputTokens,
      },
    }),
  });

  if (!response.ok) {
    const bodyText = await response.text();
    throw new Error(
      `Gemini API error: ${response.status} ${response.statusText}. ${bodyText}`
    );
  }

  const data = (await response.json()) as GeminiApiResponse;
  const responseText = data.candidates?.[0]?.content?.parts?.[0]?.text;

  if (!responseText) {
    throw new Error("Empty response from AI service");
  }

  return {
    response: responseText,
    model: GEMINI_MODEL,
  };
}
