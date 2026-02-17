package com.novachat.feature.ai.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.novachat.core.common.error.AppError
import com.novachat.core.common.error.ErrorMapper
import com.novachat.core.network.AiProxyRemoteDataSource
import com.novachat.core.network.AiProxyRequest
import com.novachat.core.network.AuthSessionProvider
import com.novachat.core.network.FirebaseFunctionsErrorMapper
import com.novachat.core.network.PlayServicesChecker
import com.novachat.core.network.chat.ChatSubmitApi
import com.novachat.core.network.chat.ChatSubmitFailoverClient
import com.novachat.core.network.chat.ChatSubmitRequest
import com.novachat.feature.ai.data.chat.ChatStatusObserver
import com.novachat.feature.ai.data.observability.ChatObservability
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.model.RequestCompletionState
import com.novachat.feature.ai.domain.model.SubmitRequest
import com.novachat.feature.ai.domain.model.SubmitResult
import com.novachat.feature.ai.domain.offline.OfflineAiEngine
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.AiServiceStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AiRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val offlineAiEngine: OfflineAiEngine,
    private val chatStatusObserver: ChatStatusObserver,
    private val chatObservability: ChatObservability
) : AiRepository {

    private val aiProxyRemoteDataSource = AiProxyRemoteDataSource()
    private val authSessionProvider = AuthSessionProvider()
    private val playServicesChecker = PlayServicesChecker()

    private val serviceStatusFlow = MutableStateFlow<AiServiceStatus>(
        AiServiceStatus.Available(AiMode.DEFAULT_MODEL_NAME)
    )

    override suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be blank"))
        }

        val validationResult = configuration.validate()
        if (validationResult.isFailure) {
            return Result.failure(
                validationResult.exceptionOrNull()
                    ?: IllegalStateException("Invalid configuration")
            )
        }

        return when (configuration.mode) {
            AiMode.ONLINE -> generateOnlineResponse(message, configuration)
            AiMode.OFFLINE -> generateOfflineResponse(message, configuration)
        }
    }

    private suspend fun generateOnlineResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val playServicesStatus = playServicesChecker.checkAvailability(context)
            if (playServicesStatus.isFailure) {
                val error = playServicesStatus.exceptionOrNull()
                    ?: SecurityException("Google Play Services unavailable")
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                return@withContext Result.failure(error)
            }

            val authStatus = authSessionProvider.ensureAuthenticated()
            if (authStatus.isFailure) {
                val error = authStatus.exceptionOrNull()
                    ?: SecurityException("Unable to authenticate with Firebase")
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                return@withContext Result.failure(error)
            }

            val responseResult = aiProxyRemoteDataSource.generateResponse(
                request = AiProxyRequest(
                    message = message,
                    temperature = configuration.modelParameters.temperature,
                    topK = configuration.modelParameters.topK,
                    topP = configuration.modelParameters.topP,
                    maxOutputTokens = configuration.modelParameters.maxOutputTokens
                )
            )

            responseResult.fold(
                onSuccess = { response ->
                    updateServiceStatus(
                        AiServiceStatus.Available(response.model ?: AiMode.DEFAULT_MODEL_NAME)
                    )
                    Result.success(response.response)
                },
                onFailure = { throwable ->
                    val mappedAppError = mapOnlineAppError(throwable)
                    val mapped = mapOnlineThrowable(mappedAppError, throwable)
                    updateServiceStatus(
                        AiServiceStatus.Error(
                            error = mapped,
                            isRecoverable = isRecoverable(throwable, mappedAppError)
                        )
                    )
                    Result.failure(mapped)
                }
            )
        }
    }

    private suspend fun generateOfflineResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = offlineAiEngine.generateResponse(message, configuration)
            result.onFailure { throwable ->
                if (throwable is UnsupportedOperationException) {
                    val reason = (observeOfflineCapability().first() as? OfflineCapability.Unavailable)
                        ?.reason
                        ?: "Offline engine unavailable"
                    updateServiceStatus(AiServiceStatus.Unavailable(reason))
                } else {
                    updateServiceStatus(
                        AiServiceStatus.Error(
                            error = throwable,
                            isRecoverable = true
                        )
                    )
                }
            }
            result
        }
    }

    override suspend fun isModeAvailable(mode: AiMode): Boolean {
        return when (mode) {
            AiMode.ONLINE -> playServicesChecker.checkAvailability(context).isSuccess
            AiMode.OFFLINE -> observeOfflineCapability().first() is OfflineCapability.Available
        }
    }

    override fun observeServiceStatus(): Flow<AiServiceStatus> {
        return serviceStatusFlow.asStateFlow()
    }

    override fun observeOfflineCapability(): Flow<OfflineCapability> {
        return offlineAiEngine.observeCapability()
    }

    override suspend fun submitAsync(request: SubmitRequest): Result<SubmitResult> =
        withContext(Dispatchers.IO) {
            val tokenResult = authSessionProvider.getIdToken()
            if (tokenResult.isFailure) return@withContext Result.failure(
                tokenResult.exceptionOrNull() ?: Exception("Not authenticated")
            )
            val token = tokenResult.getOrNull() ?: return@withContext Result.failure(Exception("No token"))
            val projectId = try {
                FirebaseApp.getInstance().options.projectId ?: "unknown"
            } catch (_: Exception) {
                "unknown"
            }
            val baseUrls = listOf(
                "https://us-central1-$projectId.cloudfunctions.net/chatSubmitPrimary",
                "https://us-east1-$projectId.cloudfunctions.net/chatSubmitSecondary"
            )
            val api = ChatSubmitApi(authToken = token, appCheckToken = null)
            val client = ChatSubmitFailoverClient(baseUrls, api)
            val req = ChatSubmitRequest(
                requestId = request.requestId,
                conversationId = request.conversationId,
                messageId = request.messageId,
                messageText = request.messageText,
                modelProfile = request.modelProfile,
                clientTsMs = request.clientTsMs,
                appInstanceId = request.appInstanceId
            )
            chatObservability.emit("submit_start", mapOf("request_id" to request.requestId))
            client.submit(req).map { resp ->
                chatObservability.emit("ack_received", mapOf("request_id" to resp.requestId, "region" to resp.region))
                if (!resp.region.contains("central1")) {
                    chatObservability.emit("failover_switch", mapOf("request_id" to resp.requestId, "region" to resp.region))
                }
                SubmitResult(
                    requestId = resp.requestId,
                    status = resp.status,
                    region = resp.region,
                    degraded = resp.degraded,
                    etaMs = resp.etaMs
                )
            }
        }

    override fun observeCompletion(requestId: String): Flow<RequestCompletionState> =
        chatStatusObserver.observe(requestId).map { s ->
            RequestCompletionState(
                requestId = s.requestId,
                state = s.state,
                attempt = s.attempt,
                errorCode = s.errorCode,
                responseText = s.responseText
            )
        }

    private fun updateServiceStatus(status: AiServiceStatus) {
        serviceStatusFlow.value = status
    }

    private fun isRecoverable(throwable: Throwable, appError: AppError): Boolean {
        FirebaseFunctionsErrorMapper.isRecoverable(throwable)?.let { return it }
        return when (appError) {
            is AppError.Validation -> false
            else -> true
        }
    }

    private fun mapOnlineAppError(throwable: Throwable): AppError {
        return FirebaseFunctionsErrorMapper.map(throwable) ?: ErrorMapper.map(throwable)
    }

    private fun mapOnlineThrowable(mapped: AppError, throwable: Throwable): Exception {
        val message = when (mapped) {
            is AppError.Network -> "Network error. Please check your connection and try again."
            is AppError.Unauthorized -> mapped.message
            is AppError.Validation -> mapped.message
            is AppError.ServiceUnavailable -> mapped.message
            is AppError.NotFound -> mapped.message
            is AppError.Unknown -> "Unexpected error. Please try again."
        }

        return Exception(message, throwable)
    }
}
