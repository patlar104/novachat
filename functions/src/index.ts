import * as functions from "firebase-functions/v2";
import { aiProxy, initializeFunctionsAdmin } from "./functions/aiProxy";
import { createChatSubmitHandler } from "./functions/chatSubmit";
import { createChatStatusHandler } from "./functions/chatStatus";
import { createChatWorkerHandler } from "./functions/chatWorker";

initializeFunctionsAdmin();

export { aiProxy };

const submitOptions = {
  region: "us-central1" as const,
  memory: "512MiB" as const,
  timeoutSeconds: 60,
  minInstances: 1,
  maxInstances: 100,
  concurrency: 40,
};

const statusOptions = {
  region: "us-central1" as const,
  memory: "256MiB" as const,
  timeoutSeconds: 30,
};

const workerOptions = {
  region: "us-central1" as const,
  memory: "1GiB" as const,
  timeoutSeconds: 60,
  minInstances: 2,
  maxInstances: 200,
  concurrency: 20,
};

export const chatSubmitPrimary = functions.https.onRequest(
  { ...submitOptions, region: "us-central1" },
  createChatSubmitHandler("us-central1", "chat-jobs-p")
);

export const chatSubmitSecondary = functions.https.onRequest(
  { ...submitOptions, region: "us-east1" },
  createChatSubmitHandler("us-east1", "chat-jobs-s")
);

export const chatStatusPrimary = functions.https.onRequest(
  { ...statusOptions, region: "us-central1" },
  createChatStatusHandler()
);

export const chatStatusSecondary = functions.https.onRequest(
  { ...statusOptions, region: "us-east1" },
  createChatStatusHandler()
);

export const chatWorkerPrimary = functions.https.onRequest(
  { ...workerOptions, region: "us-central1" },
  createChatWorkerHandler()
);

export const chatWorkerSecondary = functions.https.onRequest(
  { ...workerOptions, region: "us-east1" },
  createChatWorkerHandler()
);
