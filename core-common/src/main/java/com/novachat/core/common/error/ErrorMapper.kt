package com.novachat.core.common.error

import java.io.IOException

/**
 * Maps arbitrary exceptions to AppError for consistent handling.
 */
object ErrorMapper {
    fun map(throwable: Throwable): AppError {
        val message = throwable.message ?: "Unexpected error"
        return when (throwable) {
            is IOException -> AppError.Network(message, throwable)
            is SecurityException -> AppError.Unauthorized(message, throwable)
            is IllegalArgumentException -> AppError.Validation(message, throwable)
            else -> AppError.Unknown(message, throwable)
        }
    }
}
