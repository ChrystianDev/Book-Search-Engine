package com.booksearch.data.cache

import com.booksearch.data.fakes.FakeTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryCacheTest {

    private val time = FakeTimeProvider()
    private val ttl = 3 * 60 * 1_000L
    private val cache = InMemoryCache<String, String>(time, ttlMillis = ttl, maxEntries = 3)

    @Test
    fun `given a stored value, when read within the ttl, then it is returned`() = runTest {
        // Given
        cache.put("tolkien", "result")
        time.advanceBy(ttl - 1)

        // When / Then
        assertEquals("result", cache.get("tolkien"))
    }

    @Test
    fun `given a stored value, when the ttl has elapsed, then it is treated as absent and removed`() = runTest {
        // Given
        cache.put("tolkien", "result")
        time.advanceBy(ttl)

        // When
        val value = cache.get("tolkien")

        // Then
        assertNull(value)
        assertEquals(0, cache.size())
    }

    @Test
    fun `given an expired value, when it is stored again, then the ttl restarts`() = runTest {
        // Given
        cache.put("tolkien", "old")
        time.advanceBy(ttl + 1)

        // When
        cache.put("tolkien", "new")
        time.advanceBy(ttl - 1)

        // Then
        assertEquals("new", cache.get("tolkien"))
    }

    @Test
    fun `given a full cache, when a new key is stored, then the least recently used entry is evicted`() = runTest {
        // Given
        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")
        cache.get("a") // "a" becomes the most recently used

        // When
        cache.put("d", "4")

        // Then
        assertNull(cache.get("b"))
        assertEquals("1", cache.get("a"))
        assertEquals("3", cache.get("c"))
        assertEquals("4", cache.get("d"))
    }

    @Test
    fun `given many concurrent writers, when they store distinct keys, then no entry is lost`() = runTest {
        // Given
        val bigCache = InMemoryCache<Int, Int>(time, ttlMillis = ttl, maxEntries = 10_000)

        // When: real threads, to exercise the Mutex under contention
        withContext(Dispatchers.Default) {
            repeat(1_000) { i -> launch { bigCache.put(i, i) } }
        }

        // Then
        assertEquals(1_000, bigCache.size())
    }
}
