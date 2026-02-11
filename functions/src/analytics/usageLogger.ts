import * as admin from "firebase-admin";

export interface AiUsageLog {
  userId: string;
  messageLength: number;
  responseLength: number;
}

export async function logAiUsage(entry: AiUsageLog): Promise<void> {
  try {
    await admin.firestore().collection("ai_usage").add({
      userId: entry.userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      messageLength: entry.messageLength,
      responseLength: entry.responseLength,
    });
  } catch {
    // Logging should never break serving path.
  }
}
