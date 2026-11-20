package com.booksearch.data.repository

import android.database.sqlite.SQLiteException
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
import org.junit.Assert.assertFalse
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

    @Test
    fun `given the disk storage fails, when searching, then the result comes from the network and nothing crashes`() = runTest {
        // Given
        dao.failure = SQLiteException("disk I/O error")
        val repository = createRepository(UserPreferences(cacheType = CacheType.DISK))

        // When: both the read (miss) and the write (skipped) fail
        val first = repository.searchBooks("tolkien")
        val second = repository.searchBooks("tolkien")

        // Then: every search falls back to the network
        assertFalse(first.fromCache)
        assertFalse(second.fromCache)
        assertEquals(2, api.searchCalls.size)
        assertEquals(1, first.books.size)
    }

    @Test
    fun `given the disk storage recovers, when searching again, then the cache works again`() = runTest {
        // Given
        dao.failure = SQLiteException("database is locked")
        val repository = createRepository(UserPreferences(cacheType = CacheType.DISK))
        repository.searchBooks("tolkien")

        // When
        dao.failure = null
        repository.searchBooks("tolkien")
        val third = repository.searchBooks("tolkien")

        // Then
        assertTrue(third.fromCache)
        assertEquals(2, api.searchCalls.size)
    }
}
