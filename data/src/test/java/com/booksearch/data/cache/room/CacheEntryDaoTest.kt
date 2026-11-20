package com.booksearch.data.cache.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Runs the real Room-generated DAO against an in-memory SQLite database (via Robolectric),
 * so the SQL in [CacheEntryDao] is verified, not just a fake's interpretation of it.
 */
@RunWith(RobolectricTestRunner::class)
class CacheEntryDaoTest {

    private lateinit var database: BookSearchDatabase
    private lateinit var dao: CacheEntryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookSearchDatabase::class.java,
        ).build()
        dao = database.cacheEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entry(scope: String, key: String, storedAt: Long, json: String = "[]") =
        CacheEntryEntity(scope = scope, cacheKey = key, json = json, storedAtMillis = storedAt)

    @Test
    fun `given an entry, when upserting the same scope and key, then it is replaced`() = runTest {
        // Given
        dao.upsert(entry("search", "hobbit", storedAt = 1, json = "old"))

        // When
        dao.upsert(entry("search", "hobbit", storedAt = 2, json = "new"))

        // Then
        assertEquals(entry("search", "hobbit", storedAt = 2, json = "new"), dao.get("search", "hobbit"))
        assertEquals(1, dao.count("search"))
    }

    @Test
    fun `given the same key in two scopes, when stored, then both entries coexist`() = runTest {
        // Given / When
        dao.upsert(entry("search", "OL1W", storedAt = 1, json = "search"))
        dao.upsert(entry("detail", "OL1W", storedAt = 1, json = "detail"))

        // Then
        assertEquals("search", dao.get("search", "OL1W")?.json)
        assertEquals("detail", dao.get("detail", "OL1W")?.json)
    }

    @Test
    fun `given entries in several scopes, when getting all of one scope, then only that scope is returned`() = runTest {
        // Given
        dao.upsert(entry("search", "a", storedAt = 1))
        dao.upsert(entry("search", "b", storedAt = 2))
        dao.upsert(entry("home", "a", storedAt = 3))

        // When
        val keys = dao.getAll("search").map { it.cacheKey }.toSet()

        // Then
        assertEquals(setOf("a", "b"), keys)
        assertEquals(2, dao.count("search"))
        assertEquals(1, dao.count("home"))
    }

    @Test
    fun `given an entry, when deleted, then only that scope and key disappear`() = runTest {
        // Given
        dao.upsert(entry("search", "a", storedAt = 1))
        dao.upsert(entry("home", "a", storedAt = 1))

        // When
        dao.delete("search", "a")

        // Then
        assertNull(dao.get("search", "a"))
        assertEquals(1, dao.count("home"))
    }

    @Test
    fun `given several entries, when deleting the oldest, then the oldest of that scope go first`() = runTest {
        // Given: inserted out of order to make sure stored_at_millis decides, not insertion order
        dao.upsert(entry("search", "newest", storedAt = 30))
        dao.upsert(entry("search", "oldest", storedAt = 10))
        dao.upsert(entry("search", "middle", storedAt = 20))
        dao.upsert(entry("home", "older-than-all", storedAt = 1))

        // When
        dao.deleteOldest("search", 2)

        // Then
        assertEquals(listOf("newest"), dao.getAll("search").map { it.cacheKey })
        assertEquals(1, dao.count("home"))
    }

    @Test
    fun `given entries in several scopes, when clearing all, then the table is empty`() = runTest {
        // Given
        dao.upsert(entry("search", "a", storedAt = 1))
        dao.upsert(entry("detail", "b", storedAt = 1))

        // When
        dao.clearAll()

        // Then
        assertEquals(0, dao.count("search"))
        assertEquals(0, dao.count("detail"))
    }
}
