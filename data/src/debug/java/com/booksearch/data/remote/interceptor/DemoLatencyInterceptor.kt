package com.booksearch.data.remote.interceptor

import com.booksearch.data.LOG_TAG
import com.booksearch.data.di.ApplicationScope
import com.booksearch.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEBUG ONLY demo tool: adds an artificial delay before each API request so search
 * cancellation is visible even when Open Library answers quickly.
 *
 * Disabled by default; toggled from Settings. The delay is split in short slices and
 * checks whether OkHttp cancelled the call, so a cancelled search stops waiting at once
 * and the request never reaches the network.
 */
@Singleton
class DemoLatencyInterceptor @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationScope applicationScope: CoroutineScope,
) : Interceptor {

    // Interceptors run on OkHttp threads, so the flag is read from a volatile field
    // kept in sync with DataStore instead of blocking on the Flow.
    @Volatile
    private var enabled = false

    init {
        preferencesRepository.preferences
            .map { it.demoLatencyEnabled }
            .onEach { enabled = it }
            .launchIn(applicationScope)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (enabled) {
            val url = chain.request().url
            Timber.tag(LOG_TAG).d("demo latency: delaying %d ms -> %s", DELAY_MILLIS, url.encodedPath)
            var waited = 0L
            while (waited < DELAY_MILLIS) {
                if (chain.call().isCanceled()) {
                    Timber.tag(LOG_TAG).d("demo latency: call cancelled before reaching the network -> %s", url.encodedPath)
                    throw IOException("Canceled")
                }
                Thread.sleep(SLICE_MILLIS)
                waited += SLICE_MILLIS
            }
        }
        return chain.proceed(chain.request())
    }

    private companion object {
        const val DELAY_MILLIS = 1_500L
        const val SLICE_MILLIS = 50L
    }
}
