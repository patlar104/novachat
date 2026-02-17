/**
 * In-memory circuit breaker: rolling 60s, open on failure ratio or p95, half-open after 30s.
 */

const WINDOW_MS = 60_000;
const OPEN_AFTER_MS = 30_000;
const MIN_REQUESTS = 20;
const FAILURE_RATIO_THRESHOLD = 0.5;
const P95_THRESHOLD_MS = 12_000;
const CLOSE_AFTER_SUCCESSES = 5;

type State = "closed" | "open" | "half-open";

interface Entry {
  success: boolean;
  latencyMs: number;
  at: number;
}

let state: State = "closed";
let stateChangedAt = 0;
const entries: Entry[] = [];
let halfOpenSuccessCount = 0;

function prune(now: number): void {
  const cutoff = now - WINDOW_MS;
  while (entries.length > 0 && entries[0].at < cutoff) {
    entries.shift();
  }
}

function p95(latencies: number[]): number {
  if (latencies.length === 0) return 0;
  const sorted = [...latencies].sort((a, b) => a - b);
  const idx = Math.ceil(sorted.length * 0.95) - 1;
  return sorted[Math.max(0, idx)];
}

export function getState(): State {
  return state;
}

export function recordResult(success: boolean, latencyMs: number): void {
  const now = Date.now();
  entries.push({ success, latencyMs, at: now });
  prune(now);

  if (state === "half-open") {
    if (success) {
      halfOpenSuccessCount++;
      if (halfOpenSuccessCount >= CLOSE_AFTER_SUCCESSES) {
        state = "closed";
        stateChangedAt = now;
      }
    } else {
      state = "open";
      stateChangedAt = now;
      halfOpenSuccessCount = 0;
    }
    return;
  }

  if (state === "closed" && entries.length >= MIN_REQUESTS) {
    const failures = entries.filter((e) => !e.success).length;
    const ratio = failures / entries.length;
    const latencies = entries.map((e) => e.latencyMs);
    const p95Ms = p95(latencies);
    if (ratio > FAILURE_RATIO_THRESHOLD || p95Ms > P95_THRESHOLD_MS) {
      state = "open";
      stateChangedAt = now;
    }
  }
}

export function allowRequest(): boolean {
  const now = Date.now();
  if (state === "closed") return true;
  if (state === "open") {
    if (now - stateChangedAt >= OPEN_AFTER_MS) {
      state = "half-open";
      stateChangedAt = now;
      halfOpenSuccessCount = 0;
      return true;
    }
    return false;
  }
  if (state === "half-open") {
    return true;
  }
  return true;
}

export function isDegraded(): boolean {
  return state === "open" || state === "half-open";
}
