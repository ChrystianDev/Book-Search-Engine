package com.booksearch.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.cache.room.BookSearchDatabase
import com.booksearch.data.cache.room.CacheEntryDao
import com.booksearch.data.preferences.PreferencesRepositoryImpl
import com.booksearch.data.repository.BookRepositoryImpl
import com.booksearch.data.repository.CacheStatusRepositoryImpl
import com.booksearch.data.time.SystemTimeProvider
import com.booksearch.domain.repository.BookRepository
import com.booksearch.domain.repository.CacheStatusRepository
import com.booksearch.domain.repository.PreferencesRepository
import com.booksearch.domain.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    abstract fun bindCacheStatusRepository(impl: CacheStatusRepositoryImpl): CacheStatusRepository

    @Binds
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    companion object {

        @Provides
        fun provideCacheConfig(): CacheConfig = CacheConfig()

        @Provides
        @Singleton
        fun provideBookSearchDatabase(
            @ApplicationContext context: Context,
        ): BookSearchDatabase =
            Room.databaseBuilder(context, BookSearchDatabase::class.java, BookSearchDatabase.NAME).build()

        @Provides
        fun provideCacheEntryDao(database: BookSearchDatabase): CacheEntryDao = database.cacheEntryDao()

        @Provides
        @Singleton
        fun providePreferencesDataStore(
            @ApplicationContext context: Context,
            @IoDispatcher ioDispatcher: CoroutineDispatcher,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(SupervisorJob() + ioDispatcher),
                produceFile = { context.preferencesDataStoreFile("user_preferences") },
            )
    }
}
