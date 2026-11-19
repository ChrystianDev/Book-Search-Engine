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
import com.booksearch.domain.model.UserPreferences
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryDiskCacheTest {

    private val ttl = 3 * 60 * 1_000L
    private val api = FakeOpenLibraryApi()
    private val time = FakeTimeProvider()
    private val dao = FakeCacheEntryDao()

    private fun TestScope.createRepository(prefs: UserPreferences) = BookRepositoryImpl(
        api = api,
        caches = BookCacheStore(
            preferencesRepository = FakePreferencesRepository(prefs),
            inMemory = InMemoryBookCaches(time, CacheConfig(ttlMillis = ttl)),
            onDisk = RoomBookCaches(dao, time, CacheConfig(ttlMillis = ttl)),
        ),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun `given the disk cache is selected, when searching twice, then the second result comes from disk without a new network call`() = runTest {
        // Given
        val repository = createRepository(UserPreferences(cacheType = CacheType.DISK))

        // When
        val first = repository.searchBooks("tolkien")
        val second = repository.searchBooks("Tolkien ")

        // Then
        assertEquals(1, api.searchCalls.size)
        assertTrue(first.fromCache.not())
        assertTrue(second.fromCache)
        assertEquals(1, dao.count("search"))
    }

    @Test
    fun `given a disk-cached value, when the ttl elapses, then the network is called again`() = runTest {
        // Given
        val repository = createRepository(UserPreferences(cacheType = CacheType.DISK))
        repository.searchBooks("tolkien")

        // When
        time.advanceBy(ttl + 1)
        repository.searchBooks("tolkien")

        // Then
        assertEquals(2, api.searchCalls.size)
    }
}
