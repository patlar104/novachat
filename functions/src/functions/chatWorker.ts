/**
 * HTTP worker invoked by Cloud Tasks: CAS PROCESSING, call Gemini, write COMPLETED/FAILED.
 */

import type { Request } from "firebase-functions/v2/https";
import type { Response } from "express";
import * as functions from "firebase-functions/v2";
import {
  getChatRequest,
  updateChatRequestState,
  setChatRequestCompleted,
  setChatRequestFailed,
  setChatRequestProcessing,
} from "../chat/firestore";
import { callGeminiWithTimeout } from "../chat/callGeminiWithTimeout";
import { incrementDailyQuota } from "../chat/rateLimit";
import {
  allowRequest,
  recordResult,
  getState,
  isDegraded,
} from "../chat/circuitBreaker";
import type { RequestState } from "../chat/types";

const PROCESSING_LEASE_SECONDS = 30;
const MAX_OUTPUT_TOKENS_NORMAL = 1024;
const MAX_OUTPUT_TOKENS_DEGRADED = 512;

function parseBody(req: Request): { requestId: string } | null {
  const body = req.body;
  if (typeof body !== "object" || body === null) return null;
  const requestId = (body as Record<string, unknown>).requestId;
  if (typeof requestId !== "string" || !requestId) return null;
  return { requestId };
}

export function createChatWorkerHandler() {
  return async (req: Request, res: Response): Promise<void> => {
    if (req.method !== "POST") {
      res.status(405).end();
      return;
    }

    const parsed = parseBody(req);
    if (!parsed) {
      res.status(400).json({ error: "Missing requestId in body" });
      return;
    }
    const { requestId } = parsed;
    const logCtx = { request_id: requestId };
    const startMs = Date.now();

    const snap = await getChatRequest(requestId);
    if (!snap) {
      functions.logger.warn("Worker: request not found", logCtx);
      res.status(404).json({ error: "Request not found" });
      return;
    }

    const data = snap.data();
    const state = (data?.state as RequestState) ?? "QUEUED";
    if (state !== "QUEUED" && state !== "DEFERRED") {
      functions.logger.info("Worker: skip non-queueable state", {
        ...logCtx,
        state,
      });
      res.status(200).end();
      return;
    }

    if (!allowRequest()) {
      functions.logger.warn("Worker: circuit breaker open", logCtx);
      await updateChatRequestState(requestId, "DEFERRED", {});
      res.status(200).end();
      return;
    }

    try {
      await setChatRequestProcessing(requestId, PROCESSING_LEASE_SECONDS);
    } catch (err) {
      functions.logger.warn("Worker: CAS PROCESSING failed", {
        ...logCtx,
        error: String(err),
      });
      res.status(200).end();
      return;
    }

    const degraded = isDegraded();
    const maxOutputTokens = degraded
      ? MAX_OUTPUT_TOKENS_DEGRADED
      : MAX_OUTPUT_TOKENS_NORMAL;
    const messageText = (data?.messageText as string) ?? "";
    if (!messageText) {
      await setChatRequestFailed(requestId, "MISSING_MESSAGE");
      recordResult(false, Date.now() - startMs);
      res.status(200).end();
      return;
    }

    try {
      const result = await callGeminiWithTimeout({
        messageText,
        maxOutputTokens,
        degraded,
      });
      await setChatRequestCompleted(requestId, result.response);
      const uidHash = data?.uidHash as string | undefined;
      if (uidHash) {
        await incrementDailyQuota(uidHash, result.tokenEstimate);
      }
      recordResult(true, Date.now() - startMs);
      functions.logger.info("Worker: COMPLETED", {
        ...logCtx,
        latency_ms: Date.now() - startMs,
        breaker: getState(),
      });
      res.status(200).end();
    } catch (err) {
      const errMsg = err instanceof Error ? err.message : String(err);
      const isRetryable = /timeout|unavailable|429|503|500/i.test(errMsg);
      await setChatRequestFailed(
        requestId,
        isRetryable ? "GEMINI_TIMEOUT" : "GEMINI_ERROR"
      );
      recordResult(false, Date.now() - startMs);
      functions.logger.error("Worker: FAILED", {
        ...logCtx,
        error: errMsg,
        latency_ms: Date.now() - startMs,
      });
      res.status(200).end();
    }
  };
}
