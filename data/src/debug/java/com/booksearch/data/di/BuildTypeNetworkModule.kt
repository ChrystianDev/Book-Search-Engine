package com.booksearch.data.di

import com.booksearch.data.remote.interceptor.DemoLatencyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/** Debug-only network tools: demo latency and request logging (shows cancelled calls in Logcat). */
@Module
@InstallIn(SingletonComponent::class)
object BuildTypeNetworkModule {

    @Provides
    @ElementsIntoSet
    @BuildTypeInterceptor
    fun provideDebugInterceptors(demoLatency: DemoLatencyInterceptor): Set<Interceptor> = setOf(
        demoLatency,
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC),
    )
}
