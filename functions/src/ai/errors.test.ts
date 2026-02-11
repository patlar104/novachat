import assert from "node:assert/strict";
import test from "node:test";
import { HttpsError } from "firebase-functions/v2/https";
import { mapToHttpsError } from "./errors";

test("mapToHttpsError preserves HttpsError", () => {
  const input = new HttpsError("invalid-argument", "bad request");
  const mapped = mapToHttpsError(input);

  assert.equal(mapped, input);
});

test("mapToHttpsError maps network-like errors to unavailable", () => {
  const mapped = mapToHttpsError(new Error("network timeout"));

  assert.equal(mapped.code, "unavailable");
  assert.equal(mapped.details, undefined);
});

test("mapToHttpsError does not map generic message field to invalid-argument", () => {
  const errorBody = JSON.stringify({
    error: { message: "backend overloaded" },
  });
  const mapped = mapToHttpsError(
    new Error("Gemini API error: 503 Service Unavailable. " + errorBody)
  );

  assert.equal(mapped.code, "unavailable");
  assert.equal(mapped.details, undefined);
});

test("mapToHttpsError maps explicit validation failures to invalid-argument", () => {
  const errorBody = JSON.stringify({
    error: { message: "Invalid argument: message must be non-empty" },
  });
  const mapped = mapToHttpsError(
    new Error("Gemini API error: 400 Bad Request. " + errorBody)
  );

  assert.equal(mapped.code, "invalid-argument");
  assert.equal(mapped.details, undefined);
});

test("mapToHttpsError maps quota failures to unavailable", () => {
  const errorBody = JSON.stringify({
    error: { message: "Quota exceeded" },
  });
  const mapped = mapToHttpsError(
    new Error("Gemini API error: 429 Too Many Requests. " + errorBody)
  );

  assert.equal(mapped.code, "unavailable");
  assert.equal(mapped.details, undefined);
});
