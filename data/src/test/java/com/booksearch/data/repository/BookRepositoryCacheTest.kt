package com.booksearch.data.repository

import com.booksearch.data.cache.BookCacheStore
import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.cache.InMemoryBookCaches
import com.booksearch.data.cache.room.RoomBookCaches
import com.booksearch.data.fakes.FakeCacheEntryDao
import com.booksearch.data.fakes.FakeOpenLibraryApi
import com.booksearch.data.fakes.FakePreferencesRepository
import com.booksearch.data.fakes.FakeTimeProvider
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class BookRepositoryCacheTest {

    private val ttl = 3 * 60 * 1_000L
    private val api = FakeOpenLibraryApi()
    private val time = FakeTimeProvider()

    private fun TestScope.createRepository() = BookRepositoryImpl(
        api = api,
        caches = BookCacheStore(
            preferencesRepository = FakePreferencesRepository(),
            inMemory = InMemoryBookCaches(time, CacheConfig(ttlMillis = ttl)),
            onDisk = RoomBookCaches(FakeCacheEntryDao(), time, CacheConfig(ttlMillis = ttl)),
        ),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun `given an empty cache, when searching, then calls the network and returns a fresh result`() = runTest {
        // Given
        val repository = createRepository()

        // When
        val result = repository.searchBooks("tolkien")

        // Then
        assertEquals(listOf("tolkien"), api.searchCalls)
        assertFalse(result.fromCache)
        assertEquals(1, result.books.size)
    }

    @Test
    fun `given a cached query, when searching again within the ttl, then no network call is made`() = runTest {
        // Given
        val repository = createRepository()
        val first = repository.searchBooks("tolkien")
        time.advanceBy(ttl - 1)

        // When
        val second = repository.searchBooks("tolkien")

        // Then
        assertEquals(1, api.searchCalls.size)
        assertTrue(second.fromCache)
        assertEquals(first.books, second.books)
    }

    @Test
    fun `given queries that differ only in case and spaces, when searching, then they share the cache entry`() = runTest {
        // Given
        val repository = createRepository()
        repository.searchBooks("Tolkien ")

        // When
        val result = repository.searchBooks("tolkien")

        // Then
        assertEquals(1, api.searchCalls.size)
        assertTrue(result.fromCache)
    }

    @Test
    fun `given an expired entry, when searching, then calls the network again and refreshes the cache`() = runTest {
        // Given
        val repository = createRepository()
        repository.searchBooks("tolkien")
        time.advanceBy(ttl + 1)

        // When
        val refreshed = repository.searchBooks("tolkien")
        val afterRefresh = repository.searchBooks("tolkien")

        // Then
        assertEquals(2, api.searchCalls.size)
        assertFalse(refreshed.fromCache)
        assertTrue(afterRefresh.fromCache)
        assertEquals(refreshed.books, afterRefresh.books)
    }

    @Test
    fun `given a network failure, when searching, then nothing is cached and the next search retries`() = runTest {
        // Given
        val repository = createRepository()
        api.searchFailure = IOException("offline")

        // When
        try {
            repository.searchBooks("tolkien")
            fail("Expected DomainException")
        } catch (e: DomainException) {
            assertEquals(DomainError.NoConnection, e.error)
        }
        api.searchFailure = null
        val retry = repository.searchBooks("tolkien")

        // Then
        assertEquals(2, api.searchCalls.size)
        assertFalse(retry.fromCache)
    }

    @Test
    fun `given different queries, when searching, then each one has its own cache entry`() = runTest {
        // Given
        val repository = createRepository()
        repository.searchBooks("tolkien")
        repository.searchBooks("asimov")

        // When
        val tolkien = repository.searchBooks("tolkien")
        val asimov = repository.searchBooks("asimov")

        // Then
        assertEquals(listOf("tolkien", "asimov"), api.searchCalls)
        assertTrue(tolkien.fromCache && asimov.fromCache)
        assertEquals("tolkien #1", tolkien.books.single().title)
        assertEquals("asimov #2", asimov.books.single().title)
    }
}
