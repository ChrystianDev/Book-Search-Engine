package com.booksearch.data.error

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SafeApiCallTest {

    private suspend fun errorFor(throwable: Throwable): DomainError =
        try {
            safeApiCall { throw throwable }
        } catch (e: DomainException) {
            e.error
        }

    @Test
    fun `given network failures, when mapped, then they become no connection`() = runTest {
        assertEquals(DomainError.NoConnection, errorFor(UnknownHostException()))
        assertEquals(DomainError.NoConnection, errorFor(IOException()))
    }

    @Test
    fun `given a timeout, when mapped, then it becomes timeout`() = runTest {
        assertEquals(DomainError.Timeout, errorFor(SocketTimeoutException()))
    }

    @Test
    fun `given an http error, when mapped, then it becomes a server error with its code`() = runTest {
        val httpError = HttpException(Response.error<Any>(503, "".toResponseBody()))
        assertEquals(DomainError.Server(503), errorFor(httpError))
    }

    @Test
    fun `given a malformed body, when mapped, then it becomes unknown`() = runTest {
        assertEquals(DomainError.Unknown, errorFor(SerializationException("bad json")))
    }

    @Test
    fun `given a cancellation, when calling, then it is rethrown and never mapped to an error`() = runTest {
        try {
            safeApiCall { throw CancellationException("cancelled") }
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }
}
