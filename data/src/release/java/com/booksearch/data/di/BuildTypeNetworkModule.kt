package com.booksearch.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor

/** Release builds ship no debug network tools. */
@Module
@InstallIn(SingletonComponent::class)
object BuildTypeNetworkModule {

    @Provides
    @ElementsIntoSet
    @BuildTypeInterceptor
    fun provideReleaseInterceptors(): Set<Interceptor> = emptySet()
}
