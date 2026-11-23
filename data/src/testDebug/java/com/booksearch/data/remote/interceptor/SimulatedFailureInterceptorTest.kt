package com.booksearch.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class SimulatedFailureInterceptorTest {

    // Stands in for the network: anything that gets past the interceptor answers 200.
    private val fakeNetwork = Interceptor { chain ->
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(SimulatedFailureInterceptor())
        .addInterceptor(fakeNetwork)
        .build()

    private fun statusFor(url: String): Int =
        client.newCall(Request.Builder().url(url).build()).execute().use { it.code }

    @Test
    fun `given a search containing the trigger in any case, when sent, then it gets a 404`() {
        assertEquals(404, statusFor("https://openlibrary.org/search.json?q=tolkien%20GetFail001"))
    }

    @Test
    fun `given a normal search, when sent, then it reaches the network`() {
        assertEquals(200, statusFor("https://openlibrary.org/search.json?q=tolkien"))
    }

    @Test
    fun `given the trigger outside a search, when sent, then it reaches the network`() {
        assertEquals(200, statusFor("https://openlibrary.org/works/getfail001.json"))
    }
}
