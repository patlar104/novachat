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
});
