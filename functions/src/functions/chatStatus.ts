/**
 * GET /v1/chat/status/:requestId — polling fallback for request state.
 */

import type { Request } from "firebase-functions/v2/https";
import type { Response } from "express";
import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import { chatRequestsRef, uidHash } from "../chat/firestore";
import type { ChatStatusResponse, RequestState } from "../chat/types";

function getAuthToken(req: functions.https.Request): string | null {
  const auth = req.headers.authorization;
  if (!auth || !auth.startsWith("Bearer ")) return null;
  return auth.slice(7).trim() || null;
}

export function createChatStatusHandler() {
  return async (req: Request, res: Response): Promise<void> => {
    if (req.method !== "GET") {
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

    const requestId = req.params?.requestId ?? (req.query?.requestId as string);
    if (!requestId || typeof requestId !== "string") {
      res.status(400).json({ error: "requestId required" });
      return;
    }

    const snap = await chatRequestsRef().doc(requestId).get();
    if (!snap.exists) {
      res.status(404).json({ error: "Request not found" });
      return;
    }

    const data = snap.data();
    const docUidHash = data?.uidHash;
    const expectedHash = uidHash(uid);
    if (docUidHash !== expectedHash) {
      res.status(403).json({ error: "Forbidden" });
      return;
    }

    const state = (data?.state as RequestState) ?? "QUEUED";
    const response: ChatStatusResponse = {
      requestId,
      state,
      attempt: data?.attempt,
      errorCode: data?.errorCode,
      responseText: data?.responseText,
    };
    if (data?.createdAt) {
      response.createdAt = (data.createdAt as admin.firestore.Timestamp)
        .toDate()
        .toISOString();
    }
    if (data?.updatedAt) {
      response.updatedAt = (data.updatedAt as admin.firestore.Timestamp)
        .toDate()
        .toISOString();
    }
    res.status(200).json(response);
  };
}
