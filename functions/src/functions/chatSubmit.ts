/**
 * POST /v1/chat/submit — idempotent submit, rate limits, enqueue to Cloud Tasks.
 */

import type { Request } from "firebase-functions/v2/https";
import type { Response } from "express";
import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import { HttpsError } from "firebase-functions/v2/https";
import {
  uidHash,
  chatRequestsRef,
  dedupeKeysRef,
  createDedupeKey,
  createChatRequestDoc,
} from "../chat/firestore";
import { checkRateLimits, checkDailyQuota } from "../chat/rateLimit";
import { preCheckContent } from "../chat/policy";
import { validateSubmitBody } from "../chat/validateSubmit";
import { enqueueChatWorkerTask } from "../chat/enqueueTask";
import { DEFAULT_ETA_MS } from "../chat/types";
import type { RequestState } from "../chat/types";

// Lazy so admin is only used after initializeFunctionsAdmin() in index.
const getDb = () => admin.firestore();

function getAuthToken(req: Request): string | null {
  const auth = req.headers.authorization;
  if (!auth || !auth.startsWith("Bearer ")) return null;
  return auth.slice(7).trim() || null;
}

function getClientIp(req: Request): string {
  const forwarded = req.headers["x-forwarded-for"];
  if (typeof forwarded === "string") {
    return forwarded.split(",")[0].trim();
  }
  return (
    (req.socket as { remoteAddress?: string } | undefined)?.remoteAddress ??
    "0.0.0.0"
  );
}

export function createChatSubmitHandler(region: string, queueName: string) {
  return async (req: Request, res: Response): Promise<void> => {
    const startMs = Date.now();
    const requestId = (req.body as Record<string, unknown>)?.requestId as
      | string
      | undefined;
    const logCtx = { request_id: requestId, region };

    if (req.method !== "POST") {
      res.status(405).json({ error: "Method not allowed" });
      return;
    }

    const token = getAuthToken(req);
    if (!token) {
      res.status(401).json({ error: "Authentication required" });
      return;
    }

    let uid: string;
    try {
      const decoded = await admin.auth().verifyIdToken(token);
      uid = decoded.uid;
    } catch {
      res.status(401).json({ error: "Invalid or expired token" });
      return;
    }

    let body: ReturnType<typeof validateSubmitBody>;
    try {
      body = validateSubmitBody(req.body);
    } catch (err) {
      if (err instanceof HttpsError) {
        res.status(400).json({ error: err.message });
        return;
      }
      throw err;
    }

    const policyResult = preCheckContent(body.messageText);
    if (!policyResult.allowed) {
      functions.logger.warn("Policy block", {
        ...logCtx,
        code: policyResult.code,
      });
      res.status(400).json({
        error: policyResult.message,
        code: policyResult.code,
      });
      return;
    }

    const uidHashValue = uidHash(uid);
    const ip = getClientIp(req);

    const rateResult = await checkRateLimits({
      uid,
      appInstanceId: body.appInstanceId ?? null,
      ip,
      uidHashValue,
    });
    if (!rateResult.allowed) {
      res
        .status(429)
        .json({ error: rateResult.message, code: rateResult.code });
      return;
    }
    const quotaResult = await checkDailyQuota(uidHashValue);
    if (!quotaResult.allowed) {
      res
        .status(429)
        .json({ error: quotaResult.message, code: quotaResult.code });
      return;
    }

    try {
      const existing = await getDb().runTransaction(async (tx) => {
        const dedupeRef = dedupeKeysRef().doc(body.requestId);
        const dedupeSnap = await tx.get(dedupeRef);
        if (dedupeSnap.exists) {
          const chatSnap = await tx.get(chatRequestsRef().doc(body.requestId));
          if (chatSnap.exists) {
            const data = chatSnap.data();
            return {
              requestId: body.requestId,
              status: (data?.state as RequestState) ?? "QUEUED",
              region,
              degraded: false,
              etaMs: DEFAULT_ETA_MS,
            };
          }
        }

        const dedupeData = createDedupeKey(body.requestId);
        tx.set(dedupeRef, dedupeData);

        const chatRef = chatRequestsRef().doc(body.requestId);
        const chatDoc = createChatRequestDoc({
          requestId: body.requestId,
          conversationId: body.conversationId,
          uidHash: uidHashValue,
          state: "QUEUED",
          attempt: 1,
          messageText: body.messageText,
        });
        tx.set(chatRef, chatDoc);

        return null;
      });

      if (existing) {
        res.status(202).json(existing);
        functions.logger.info("Submit dedupe hit", {
          ...logCtx,
          latency_ms: Date.now() - startMs,
        });
        return;
      }

      await enqueueChatWorkerTask({
        region,
        queueName,
        requestId: body.requestId,
      });

      const response = {
        requestId: body.requestId,
        status: "QUEUED" as RequestState,
        region,
        degraded: false,
        etaMs: DEFAULT_ETA_MS,
      };
      res.status(202).json(response);
      functions.logger.info("Submit ACK", {
        ...logCtx,
        latency_ms: Date.now() - startMs,
      });
    } catch (err) {
      if (err instanceof HttpsError) {
        const code = err.code === "resource-exhausted" ? 429 : 400;
        res.status(code).json({ error: err.message });
        return;
      }
      functions.logger.error("Submit error", { ...logCtx, error: String(err) });
      res.status(500).json({ error: "Internal error" });
    }
  };
}
