package com.booksearch.data.di

import com.booksearch.data.remote.interceptor.DemoLatencyInterceptor
import com.booksearch.data.remote.interceptor.httpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor

/**
 * Debug-only network tools: demo latency and full HTTP logging (Logcat tag `okhttp.OkHttpClient`).
 * The logger goes last, so the demo delay runs before the request is logged as sent.
 */
@Module
@InstallIn(SingletonComponent::class)
object BuildTypeNetworkModule {

    @Provides
    @ElementsIntoSet
    @BuildTypeInterceptor
    fun provideDebugInterceptors(demoLatency: DemoLatencyInterceptor): Set<Interceptor> = setOf(
        demoLatency,
        httpLoggingInterceptor(),
    )
}
