import assert from "node:assert/strict";
import test from "node:test";
import { HttpsError } from "firebase-functions/v2/https";
import { validateAiProxyRequest } from "./validateRequest";

test("validateAiProxyRequest returns defaults for missing model parameters", () => {
  const result = validateAiProxyRequest({ message: "hello" });

  assert.equal(result.message, "hello");
  assert.equal(result.modelParameters.temperature, 0.7);
  assert.equal(result.modelParameters.topK, 40);
  assert.equal(result.modelParameters.topP, 0.95);
  assert.equal(result.modelParameters.maxOutputTokens, 2048);
});

test("validateAiProxyRequest throws HttpsError for blank message", () => {
  assert.throws(
    () => validateAiProxyRequest({ message: "   " }),
    (error: unknown) => {
      assert.ok(error instanceof HttpsError);
      assert.equal(error.code, "invalid-argument");
      return true;
    }
  );
});
