package com.novachat.feature.ai.data.repository

import android.content.Context
import com.novachat.core.common.error.AppError
import com.novachat.core.common.error.ErrorMapper
import com.novachat.core.network.AiProxyRemoteDataSource
import com.novachat.core.network.AiProxyRequest
import com.novachat.core.network.AuthSessionProvider
import com.novachat.core.network.FirebaseFunctionsErrorMapper
import com.novachat.core.network.PlayServicesChecker
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
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
import kotlinx.coroutines.withContext

class AiRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val offlineAiEngine: OfflineAiEngine
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
