package com.booksearch.data.repository

import com.booksearch.data.cache.BookCacheStore
import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.cache.InMemoryBookCaches
import com.booksearch.data.cache.room.RoomBookCaches
import com.booksearch.data.fakes.FakeCacheEntryDao
import com.booksearch.data.fakes.FakeOpenLibraryApi
import com.booksearch.data.fakes.FakePreferencesRepository
import com.booksearch.data.fakes.FakeTimeProvider
import com.booksearch.domain.model.CacheType
import com.booksearch.domain.model.CachedSearch
import com.booksearch.domain.model.UserPreferences
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CacheStatusRepositoryImplTest {

    private val ttl = 3 * 60 * 1_000L
    private val api = FakeOpenLibraryApi()
    private val time = FakeTimeProvider()
    private val preferences = FakePreferencesRepository(UserPreferences())

    private val caches = BookCacheStore(
        preferencesRepository = preferences,
        inMemory = InMemoryBookCaches(time, CacheConfig(ttlMillis = ttl)),
        onDisk = RoomBookCaches(FakeCacheEntryDao(), time, CacheConfig(ttlMillis = ttl)),
    )
    private val statusRepository = CacheStatusRepositoryImpl(caches)

    private fun TestScope.bookRepository() =
        BookRepositoryImpl(api, caches, StandardTestDispatcher(testScheduler))

    @Test
    fun `given searches with and without results, when listing, then only the ones with results are returned with their expiry`() = runTest {
        // Given
        api.emptyQueries += "zzzz"
        val repository = bookRepository()
        repository.searchBooks("Tolkien")
        time.advanceBy(1_000)
        repository.searchBooks("zzzz")

        // When
        val cached = statusRepository.cachedSearches()

        // Then
        assertEquals(listOf(CachedSearch(query = "tolkien", expiresAtMillis = ttl)), cached)
    }

    @Test
    fun `given an expired search not read again, when listing, then it is still listed`() = runTest {
        // Given
        bookRepository().searchBooks("tolkien")

        // When
        time.advanceBy(ttl + 1)

        // Then: expiration is lazy, the entry is only removed when that query is read again
        assertEquals(listOf("tolkien"), statusRepository.cachedSearches().map { it.query })
    }

    @Test
    fun `given the disk cache is selected, when listing, then the disk entries are returned`() = runTest {
        // Given
        val repository = bookRepository()
        repository.searchBooks("memory only")
        preferences.setCacheType(CacheType.DISK)
        repository.searchBooks("dune")

        // When
        val cached = statusRepository.cachedSearches()

        // Then
        assertEquals(listOf("dune"), cached.map { it.query })
    }
}
