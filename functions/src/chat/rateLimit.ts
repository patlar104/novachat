/**
 * Rate limiting: token bucket per user, device, IP; in-flight cap; daily quota.
 */

import { createHash } from "crypto";
import * as admin from "firebase-admin";
import {
  ipPrefixHash,
  rateBucketShardKey,
  rateBucketsRef,
  quotaDailyRef,
  chatRequestsRef,
} from "./firestore";
import {
  RATE_LIMIT_USER_PER_MIN,
  RATE_LIMIT_DEVICE_PER_MIN,
  RATE_LIMIT_IP_PER_MIN,
  IN_FLIGHT_MAX_PER_USER,
  DAILY_TOKEN_BUDGET,
} from "./types";

const db = admin.firestore();
const nowMs = () => Date.now();
const windowMin = () => Math.floor(nowMs() / 60_000);

export type RateLimitResult =
  | { allowed: true }
  | { allowed: false; code: string; message: string };

export async function checkRateLimits(params: {
  uid: string;
  appInstanceId: string | null;
  ip: string;
  uidHashValue: string;
}): Promise<RateLimitResult> {
  const { appInstanceId, ip, uidHashValue } = params;
  const window = windowMin();

  const userKey = `user:${uidHashValue}`;
  const deviceKey = `device:${appInstanceId ? hashForDevice(appInstanceId) : "unknown"}`;
  const ipKey = `ip:${ipPrefixHash(ip)}`;

  const [userOk, deviceOk, ipOk] = await Promise.all([
    checkBucket(userKey, window, RATE_LIMIT_USER_PER_MIN),
    checkBucket(deviceKey, window, RATE_LIMIT_DEVICE_PER_MIN),
    checkBucket(ipKey, window, RATE_LIMIT_IP_PER_MIN),
  ]);

  if (!userOk) {
    return {
      allowed: false,
      code: "RATE_LIMIT_USER",
      message: "Too many requests per user.",
    };
  }
  if (!deviceOk) {
    return {
      allowed: false,
      code: "RATE_LIMIT_DEVICE",
      message: "Too many requests per device.",
    };
  }
  if (!ipOk) {
    return {
      allowed: false,
      code: "RATE_LIMIT_IP",
      message: "Too many requests from this network.",
    };
  }

  const inFlight = await countInFlightByUser(uidHashValue);
  if (inFlight >= IN_FLIGHT_MAX_PER_USER) {
    return {
      allowed: false,
      code: "IN_FLIGHT_CAP",
      message: "Too many requests in progress. Please wait.",
    };
  }

  return { allowed: true };
}

function hashForDevice(id: string): string {
  return createHash("sha256").update(id).digest("hex");
}

async function checkBucket(
  key: string,
  window: number,
  limit: number
): Promise<boolean> {
  const shard = rateBucketShardKey(key, window);
  const docId = `${key.replace(/[^a-z0-9_]/gi, "_")}_${window}_${shard}`;
  const ref = rateBucketsRef().doc(docId);

  return await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    const ttlAt = new Date();
    ttlAt.setHours(ttlAt.getHours() + 2);
    const ttlTimestamp = admin.firestore.Timestamp.fromDate(ttlAt);

    let count = 0;
    if (snap.exists) {
      const data = snap.data();
      count = (data?.count as number) ?? 0;
    }

    if (count >= limit) {
      return false;
    }

    tx.set(ref, {
      count: count + 1,
      window,
      ttlAt: ttlTimestamp,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return true;
  });
}

async function countInFlightByUser(uidHashValue: string): Promise<number> {
  const snap = await chatRequestsRef()
    .where("uidHash", "==", uidHashValue)
    .where("state", "in", ["QUEUED", "PROCESSING"])
    .limit(IN_FLIGHT_MAX_PER_USER + 1)
    .get();
  return snap.size;
}

export async function checkDailyQuota(
  uidHashValue: string
): Promise<RateLimitResult> {
  const dateStr = new Date().toISOString().slice(0, 10);
  const docId = `${dateStr}_${uidHashValue}`;
  const ref = quotaDailyRef().doc(docId);

  const snap = await ref.get();
  const used = (snap.data()?.tokensUsed as number) ?? 0;
  if (used >= DAILY_TOKEN_BUDGET) {
    return {
      allowed: false,
      code: "QUOTA_DAILY",
      message: "Daily token limit reached. Try again tomorrow.",
    };
  }
  return { allowed: true };
}

export async function incrementDailyQuota(
  uidHashValue: string,
  tokensUsed: number
): Promise<void> {
  const dateStr = new Date().toISOString().slice(0, 10);
  const docId = `${dateStr}_${uidHashValue}`;
  const ref = quotaDailyRef().doc(docId);

  const ttlAt = new Date();
  ttlAt.setDate(ttlAt.getDate() + 400);
  await ref.set(
    {
      tokensUsed: admin.firestore.FieldValue.increment(tokensUsed),
      date: dateStr,
      ttlAt: admin.firestore.Timestamp.fromDate(ttlAt),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );
}
