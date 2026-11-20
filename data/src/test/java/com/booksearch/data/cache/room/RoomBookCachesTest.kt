package com.booksearch.data.cache.room

import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.fakes.FakeCacheEntryDao
import com.booksearch.data.fakes.FakeTimeProvider
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomBookCachesTest {

    private val dao = FakeCacheEntryDao()
    private val caches = RoomBookCaches(dao, FakeTimeProvider(), CacheConfig())

    private suspend fun storeRaw(scope: String, key: String, json: String) =
        dao.upsert(CacheEntryEntity(scope = scope, cacheKey = key, json = json, storedAtMillis = 0))

    @Test
    fun `given books, when stored and read, then the same domain books are returned`() = runTest {
        // Given
        val books = listOf(Book("OL1W", "The Hobbit", listOf("J.R.R. Tolkien"), 1937, 42, null))

        // When
        caches.search.put("hobbit", books)

        // Then
        assertEquals(books, caches.search.get("hobbit"))
    }

    @Test
    fun `given a search stored by the previous version, when read, then it still decodes`() = runTest {
        // Given: JSON exactly as written when the domain model itself was serialized
        storeRaw(
            "search",
            "hobbit",
            """[{"workId":"OL1W","title":"The Hobbit","authors":["J.R.R. Tolkien"],"firstPublishYear":1937,"editionCount":42,"coverUrl":null}]""",
        )

        // When / Then
        assertEquals(
            listOf(Book("OL1W", "The Hobbit", listOf("J.R.R. Tolkien"), 1937, 42, null)),
            caches.search.get("hobbit"),
        )
    }

    @Test
    fun `given a detail stored by the previous version, when read, then it still decodes`() = runTest {
        // Given
        storeRaw(
            "detail",
            "OL1W",
            """{"workId":"OL1W","title":"The Hobbit","authors":["J.R.R. Tolkien"],"firstPublishYear":null,"description":"A hobbit.","subjects":["Fantasy"],"coverUrl":"https://covers/1.jpg"}""",
        )

        // When / Then
        assertEquals(
            BookDetail("OL1W", "The Hobbit", listOf("J.R.R. Tolkien"), null, "A hobbit.", listOf("Fantasy"), "https://covers/1.jpg"),
            caches.detail.get("OL1W"),
        )
    }

    @Test
    fun `given stored json that no longer matches the format, when read, then it is a cache miss`() = runTest {
        // Given
        storeRaw("search", "hobbit", """[{"unexpected":true}]""")

        // When / Then
        assertNull(caches.search.get("hobbit"))
    }

    @Test
    fun `given stored text that is not valid json, when read, then it is a cache miss`() = runTest {
        // Given
        storeRaw("detail", "OL1W", "not json {")

        // When / Then
        assertNull(caches.detail.get("OL1W"))
    }
}
