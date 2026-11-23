package com.booksearch.domain.error

/**
 * Runs [block] and captures only [DomainException] as a failed [Result].
 *
 * Unlike `runCatching`, any other throwable — in particular `CancellationException` —
 * propagates, so structured concurrency and cancellation keep working.
 */
inline fun <T> catchDomainError(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: DomainException) {
        Result.failure(e)
    }
