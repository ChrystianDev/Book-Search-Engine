package com.booksearch.app.di

import com.booksearch.app.BuildConfig
import com.booksearch.app.feature.home.SearchConfig
import com.booksearch.data.remote.ClientInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val CONTACT_URL = "https://gitlab.com/android-apps9344722/book-search-engine-tht"

    @Provides
    fun provideSearchConfig(): SearchConfig = SearchConfig()

    @Provides
    fun provideClientInfo(): ClientInfo = ClientInfo(
        appName = "BookSearch",
        appVersion = BuildConfig.VERSION_NAME,
        contactUrl = CONTACT_URL,
    )
}
