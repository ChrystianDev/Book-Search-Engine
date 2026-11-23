package com.booksearch.domain.error

/** Failures the UI knows how to explain. The data layer maps any technical error to one of these. */
sealed interface DomainError {
    data object NoConnection : DomainError
    data object Timeout : DomainError
    data class Server(val httpCode: Int) : DomainError
    data object Unknown : DomainError
}

/**
 * Exception carrying a [DomainError] across suspend boundaries.
 *
 * Using exceptions (instead of a Result wrapper) keeps coroutine cancellation
 * untouched: a `CancellationException` is never confused with a domain failure.
 */
class DomainException(
    val error: DomainError,
    cause: Throwable? = null,
) : Exception(error.toString(), cause)
