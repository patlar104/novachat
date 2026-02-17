/**
 * Enqueue a Cloud Task to invoke the chat worker for a requestId.
 */

import { CloudTasksClient } from "@google-cloud/tasks";

export async function enqueueChatWorkerTask(params: {
  region: string;
  queueName: string;
  requestId: string;
  workerUrl?: string;
}): Promise<void> {
  const { region, queueName, requestId, workerUrl } = params;
  const project = process.env.GCLOUD_PROJECT || process.env.GCP_PROJECT;
  if (!project) {
    throw new Error("GCLOUD_PROJECT is not set");
  }

  const url =
    workerUrl ||
    process.env[`CHAT_WORKER_URL_${region.toUpperCase().replace(/-/g, "_")}`] ||
    `https://${region}-${project}.cloudfunctions.net/chatWorker${region === "us-central1" ? "Primary" : "Secondary"}`;

  const client = new CloudTasksClient();
  const parent = client.queuePath(project, region, queueName);
  const body = Buffer.from(JSON.stringify({ requestId })).toString("base64");

  await client.createTask({
    parent,
    task: {
      httpRequest: {
        httpMethod: "POST" as const,
        url,
        headers: { "Content-Type": "application/json" },
        body,
      },
      dispatchDeadline: { seconds: 30 },
    },
  });
}
