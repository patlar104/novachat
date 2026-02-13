export interface AiProxyModelParameters {
  temperature: number;
  topK: number;
  topP: number;
  maxOutputTokens: number;
}

export interface AiProxyValidatedRequest {
  message: string;
  modelParameters: AiProxyModelParameters;
}

export interface GeminiResponse {
  response: string;
  model: string;
}
