/**
 * Types for SPEC-1 resilient chat: submit, status, and Firestore chat_requests.
 */

import type { Timestamp } from "firebase-admin/firestore";

export type RequestState =
  | "QUEUED"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "DEFERRED";

export type ModelProfile = "standard" | "cheap";

export interface ChatSubmitRequest {
  requestId: string;
  conversationId: string;
  messageId: string;
  messageText: string;
  modelProfile: ModelProfile;
  clientTsMs: number;
  appInstanceId?: string | null;
}

export interface ChatSubmitResponse {
  requestId: string;
  status: RequestState;
  region: string;
  degraded: boolean;
  etaMs: number;
}

export interface ChatStatusResponse {
  requestId: string;
  state: RequestState;
  attempt?: number;
  errorCode?: string;
  responseText?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ChatRequestDocument {
  requestId: string;
  conversationId: string;
  uidHash: string;
  state: RequestState;
  attempt: number;
  errorCode?: string;
  responseText?: string;
  createdAt: Timestamp;
  updatedAt: Timestamp;
  ttlAt: Timestamp;
  processingLeaseExpiresAt?: Timestamp;
}

export const MAX_MESSAGE_LENGTH = 4000;
export const DEFAULT_ETA_MS = 3000;
export const RATE_LIMIT_USER_PER_MIN = 10;
export const RATE_LIMIT_DEVICE_PER_MIN = 20;
export const RATE_LIMIT_IP_PER_MIN = 60;
export const IN_FLIGHT_MAX_PER_USER = 2;
export const DAILY_TOKEN_BUDGET = 50_000;
export const RATE_BUCKET_SHARDS = 32;
export const DEDUPE_TTL_HOURS = 24;
export const CHAT_REQUEST_TTL_HOURS = 24;
