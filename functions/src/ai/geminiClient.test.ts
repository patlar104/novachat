import assert from "node:assert/strict";
import test from "node:test";
import { callGemini } from "./geminiClient";

test("callGemini sends API key in header and not URL query params", async () => {
  const originalFetch = globalThis.fetch;
  let capturedUrl = "";
  let capturedHeaders: Headers | undefined;

  try {
    globalThis.fetch = (async (input, init) => {
      capturedUrl = String(input);
      capturedHeaders = new Headers(init?.headers);
      return new Response(
        JSON.stringify({
          candidates: [{ content: { parts: [{ text: "ok" }] } }],
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }
      );
    }) as typeof fetch;

    const result = await callGemini("AIzaSecretForTest", {
      message: "hello",
      modelParameters: {
        temperature: 0.7,
        topK: 40,
        topP: 0.95,
        maxOutputTokens: 128,
      },
    });

    assert.equal(result.response, "ok");
    assert.equal(capturedUrl.includes("?key="), false);
    assert.equal(capturedHeaders?.get("x-goog-api-key"), "AIzaSecretForTest");
  } finally {
    globalThis.fetch = originalFetch;
  }
});
