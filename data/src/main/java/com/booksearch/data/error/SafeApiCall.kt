package com.booksearch.data.error

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a network call and translates technical failures into [DomainException].
 *
 * `CancellationException` is rethrown untouched: cancellation is not an error and
 * must keep propagating so structured concurrency works. If the coroutine was
 * cancelled while OkHttp reported an `IOException("Canceled")`, [ensureActive]
 * turns it back into a cancellation instead of a fake "no connection" error.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        throw e.toDomainException()
    }

internal fun Throwable.toDomainException(): DomainException = when (this) {
    is DomainException -> this
    else -> DomainException(toDomainError(), cause = this)
}

private fun Throwable.toDomainError(): DomainError = when (this) {
    is SocketTimeoutException -> DomainError.Timeout
    // OkHttp reports call timeouts as a plain InterruptedIOException("timeout").
    is InterruptedIOException -> DomainError.Timeout
    is IOException -> DomainError.NoConnection
    is HttpException -> DomainError.Server(code())
    is SerializationException -> DomainError.Unknown
    else -> DomainError.Unknown
}
