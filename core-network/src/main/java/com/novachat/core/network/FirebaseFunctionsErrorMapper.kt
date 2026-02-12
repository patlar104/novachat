package com.novachat.core.network

import com.google.firebase.functions.FirebaseFunctionsException
import com.novachat.core.common.error.AppError

/**
 * Maps Firebase callable-function failures to app-level errors without leaking
 * Firebase-specific types into feature modules.
 */
object FirebaseFunctionsErrorMapper {
    fun map(throwable: Throwable): AppError? {
        val error = throwable as? FirebaseFunctionsException ?: return null
        return when (error.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED -> {
                AppError.Unauthorized(
                    message = "Authentication required. Please sign in and retry.",
                    cause = throwable
                )
            }
            FirebaseFunctionsException.Code.PERMISSION_DENIED -> {
                AppError.Unauthorized(
                    message = "Permission denied. Please check your account access.",
                    cause = throwable
                )
            }
            FirebaseFunctionsException.Code.INVALID_ARGUMENT -> {
                AppError.Validation(
                    message = "Invalid request. Please check your input and try again.",
                    cause = throwable
                )
            }
            FirebaseFunctionsException.Code.FAILED_PRECONDITION -> {
                AppError.ServiceUnavailable(
                    message = "Service configuration error. Please contact support.",
                    cause = throwable
                )
            }
            FirebaseFunctionsException.Code.NOT_FOUND -> {
                AppError.NotFound(
                    message = "AI service endpoint was not found. Please try again later.",
                    cause = throwable
                )
            }
            FirebaseFunctionsException.Code.UNAVAILABLE,
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED,
            FirebaseFunctionsException.Code.INTERNAL,
            FirebaseFunctionsException.Code.ABORTED,
            FirebaseFunctionsException.Code.CANCELLED,
            FirebaseFunctionsException.Code.UNKNOWN -> {
                AppError.ServiceUnavailable(
                    message = "AI service is temporarily unavailable. Please try again shortly.",
                    cause = throwable
                )
            }
            else -> {
                AppError.Unknown(
                    message = throwable.message ?: "Unexpected error",
                    cause = throwable
                )
            }
        }
    }

    /**
     * Returns recoverability for Firebase callable-function errors.
     *
     * This restores prior behavior where permission-denied is treated as
     * non-recoverable while other Firebase failures are retryable.
     */
    fun isRecoverable(throwable: Throwable): Boolean? {
        val error = throwable as? FirebaseFunctionsException ?: return null
        return error.code != FirebaseFunctionsException.Code.PERMISSION_DENIED
    }
}
