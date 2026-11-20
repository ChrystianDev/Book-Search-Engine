package com.booksearch.data.cache.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.booksearch.data.fakes.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** [RoomCache]'s TTL and entry limit on a real (in-memory) Room database. */
@RunWith(RobolectricTestRunner::class)
class RoomCacheIntegrationTest {

    private val ttl = 3 * 60 * 1_000L
    private val time = FakeTimeProvider()
    private lateinit var database: BookSearchDatabase
    private lateinit var cache: RoomCache<String>

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookSearchDatabase::class.java,
        ).build()
        cache = RoomCache(
            dao = database.cacheEntryDao(),
            scope = "search",
            timeProvider = time,
            ttlMillis = ttl,
            maxEntries = 2,
            encode = { it },
            decode = { it },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `given more entries than the limit, when putting, then the oldest stored is evicted`() = runTest {
        // Given
        cache.put("a", "A")
        time.advanceBy(1)
        cache.put("b", "B")
        time.advanceBy(1)

        // When
        cache.put("c", "C")

        // Then
        assertNull(cache.get("a"))
        assertEquals("B", cache.get("b"))
        assertEquals("C", cache.get("c"))
    }

    @Test
    fun `given an entry older than the ttl, when read, then it is a miss and is deleted`() = runTest {
        // Given
        cache.put("a", "A")

        // When
        time.advanceBy(ttl)

        // Then
        assertNull(cache.get("a"))
        assertEquals(0, database.cacheEntryDao().count("search"))
    }
}
