package com.booksearch.data.di

import com.booksearch.data.remote.interceptor.DemoLatencyInterceptor
import com.booksearch.data.remote.interceptor.SimulatedFailureInterceptor
import com.booksearch.data.remote.interceptor.httpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor

/**
 * Debug-only network tools: demo latency, full HTTP logging (Logcat tag `okhttp.OkHttpClient`) and
 * the simulated failure for searches containing `getfail001`. Order matters: the demo delay runs
 * before the request is logged as sent, and the simulated 404 comes after the logger so it is logged too.
 */
@Module
@InstallIn(SingletonComponent::class)
object BuildTypeNetworkModule {

    @Provides
    @ElementsIntoSet
    @BuildTypeInterceptor
    fun provideDebugInterceptors(
        demoLatency: DemoLatencyInterceptor,
        simulatedFailure: SimulatedFailureInterceptor,
    ): Set<Interceptor> = setOf(
        demoLatency,
        httpLoggingInterceptor(),
        simulatedFailure,
    )
}
