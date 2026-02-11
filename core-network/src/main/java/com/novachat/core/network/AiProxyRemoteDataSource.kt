package com.novachat.core.network

data class AiProxyRequest(
    val message: String,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxOutputTokens: Int
)

data class AiProxyResponse(
    val response: String,
    val model: String?
)

class AiProxyRemoteDataSource(
    private val callableClient: FirebaseCallableClient = FirebaseCallableClient()
) {
    suspend fun generateResponse(request: AiProxyRequest): Result<AiProxyResponse> {
        val payload = hashMapOf(
            "message" to request.message,
            "modelParameters" to hashMapOf(
                "temperature" to request.temperature,
                "topK" to request.topK,
                "topP" to request.topP,
                "maxOutputTokens" to request.maxOutputTokens
            )
        )

        return callableClient.call("aiProxy", payload).mapCatching { data ->
            val responseText = data["response"] as? String
            if (responseText.isNullOrBlank()) {
                throw IllegalStateException("AI returned empty response")
            }
            AiProxyResponse(
                response = responseText,
                model = data["model"] as? String
            )
        }
    }
}
