package com.booksearch.data.remote.interceptor

import com.booksearch.data.LOG_TAG
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import javax.inject.Inject

/**
 * DEBUG ONLY demo tool: a search whose text contains [TRIGGER] (any case) gets a 404 without
 * reaching the network, because the real API cannot be made to fail on demand.
 *
 * The fake response goes through the normal path (Retrofit `HttpException` → `DomainError.Server(404)`),
 * so the search screen shows its error state and the search log records a failure.
 */
class SimulatedFailureInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val query = request.url.queryParameter("q").orEmpty()
        if (request.url.encodedPath != SEARCH_PATH || !query.contains(TRIGGER, ignoreCase = true)) {
            return chain.proceed(request)
        }
        Timber.tag(LOG_TAG).d("simulated failure: \"%s\" -> HTTP 404", query)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found (simulated)")
            .body("""{"error":"simulated failure for '$TRIGGER'"}""".toResponseBody("application/json".toMediaType()))
            .build()
    }

    companion object {
        const val TRIGGER = "getfail001"
        private const val SEARCH_PATH = "/search.json"
    }
}
