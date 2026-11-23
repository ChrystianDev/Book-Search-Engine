package com.booksearch.data.di

import com.booksearch.data.remote.ClientInfo
import com.booksearch.data.remote.NetworkJson
import com.booksearch.data.remote.OpenLibraryApi
import com.booksearch.data.remote.interceptor.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 20L

    @Provides
    @Singleton
    @ImageHttpClient
    fun provideImageHttpClient(clientInfo: ClientInfo): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor(clientInfo.userAgent))
            .build()

    @Provides
    @Singleton
    fun provideApiHttpClient(
        @ImageHttpClient baseClient: OkHttpClient,
        @BuildTypeInterceptor buildTypeInterceptors: Set<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient =
        baseClient.newBuilder()
            .apply { buildTypeInterceptors.forEach(::addInterceptor) }
            .build()

    @Provides
    @Singleton
    fun provideOpenLibraryApi(client: OkHttpClient): OpenLibraryApi =
        Retrofit.Builder()
            .baseUrl(OpenLibraryApi.BASE_URL)
            .client(client)
            .addConverterFactory(NetworkJson.instance.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenLibraryApi::class.java)
}
