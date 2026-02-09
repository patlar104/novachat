package com.novachat.core.common.error

/**
 * Canonical error types shared across the app.
 */
sealed interface AppError {
    val message: String
    val cause: Throwable?

    data class Network(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class Unauthorized(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class NotFound(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class ServiceUnavailable(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class Validation(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError

    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError
}
