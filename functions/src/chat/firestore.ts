/**
 * Firestore helpers for chat_requests, dedupe_keys, rate_buckets, quota_daily.
 */

import * as admin from "firebase-admin";
import { createHash, createHmac } from "crypto";
import type { Timestamp } from "firebase-admin/firestore";
import type { RequestState } from "./types";
import {
  CHAT_REQUEST_TTL_HOURS,
  DEDUPE_TTL_HOURS,
  RATE_BUCKET_SHARDS,
} from "./types";

const db = () => admin.firestore();

const HASH_SECRET =
  process.env.CHAT_UID_HMAC_SECRET ?? "default-secret-rotate-me";

export function uidHash(uid: string): string {
  return createHmac("sha256", HASH_SECRET).update(uid).digest("hex");
}

export function ipPrefixHash(ip: string): string {
  const parts = ip.trim().split(".");
  if (parts.length >= 4) {
    parts[3] = "0";
    return createHash("sha256").update(parts.join(".")).digest("hex");
  }
  return createHash("sha256").update(ip).digest("hex");
}

export function rateBucketShardKey(key: string, windowMin: number): number {
  const composite = `${key}:${windowMin}`;
  let h = 0;
  for (let i = 0; i < composite.length; i++) {
    h = (h * 31 + composite.charCodeAt(i)) >>> 0;
  }
  return h % RATE_BUCKET_SHARDS;
}

function ttlHoursFromNow(hours: number): Timestamp {
  const d = new Date();
  d.setHours(d.getHours() + hours);
  return admin.firestore.Timestamp.fromDate(d);
}

export function chatRequestsRef() {
  return db().collection("chat_requests");
}

export function dedupeKeysRef() {
  return db().collection("dedupe_keys");
}

export function rateBucketsRef() {
  return db().collection("rate_buckets");
}

export function quotaDailyRef() {
  return db().collection("quota_daily");
}

export function createDedupeKey(
  requestId: string,
  ttlHours: number = DEDUPE_TTL_HOURS
): { requestId: string; ttlAt: Timestamp } {
  return {
    requestId,
    ttlAt: ttlHoursFromNow(ttlHours),
  };
}

export function createChatRequestDoc(params: {
  requestId: string;
  conversationId: string;
  uidHash: string;
  state: RequestState;
  attempt?: number;
  messageText?: string;
}): Record<string, unknown> {
  const now = admin.firestore.Timestamp.now();
  const ttlAt = ttlHoursFromNow(CHAT_REQUEST_TTL_HOURS);
  const doc: Record<string, unknown> = {
    requestId: params.requestId,
    conversationId: params.conversationId,
    uidHash: params.uidHash,
    state: params.state,
    attempt: params.attempt ?? 1,
    createdAt: now,
    updatedAt: now,
    ttlAt,
  };
  if (params.messageText !== undefined) {
    doc.messageText = params.messageText;
  }
  return doc;
}

export async function getChatRequest(
  requestId: string
): Promise<admin.firestore.DocumentSnapshot | null> {
  const snap = await chatRequestsRef().doc(requestId).get();
  return snap.exists ? snap : null;
}

export async function updateChatRequestState(
  requestId: string,
  state: RequestState,
  updates: {
    responseText?: string;
    errorCode?: string;
    attempt?: number;
    processingLeaseExpiresAt?: Timestamp | null;
  } = {}
): Promise<void> {
  const ref = chatRequestsRef().doc(requestId);
  const now = admin.firestore.Timestamp.now();
  const data: Record<string, unknown> = {
    state,
    updatedAt: now,
    ...updates,
  };
  if (updates.processingLeaseExpiresAt === null) {
    data.processingLeaseExpiresAt = admin.firestore.FieldValue.delete();
  }
  await ref.update(data);
}

export async function setChatRequestCompleted(
  requestId: string,
  responseText: string
): Promise<void> {
  await updateChatRequestState(requestId, "COMPLETED", {
    responseText,
    processingLeaseExpiresAt: null,
  });
}

export async function setChatRequestFailed(
  requestId: string,
  errorCode: string
): Promise<void> {
  await updateChatRequestState(requestId, "FAILED", {
    errorCode,
    processingLeaseExpiresAt: null,
  });
}

export async function setChatRequestProcessing(
  requestId: string,
  leaseSeconds: number
): Promise<void> {
  const leaseEnd = new Date();
  leaseEnd.setSeconds(leaseEnd.getSeconds() + leaseSeconds);
  await updateChatRequestState(requestId, "PROCESSING", {
    processingLeaseExpiresAt: admin.firestore.Timestamp.fromDate(leaseEnd),
  });
}
