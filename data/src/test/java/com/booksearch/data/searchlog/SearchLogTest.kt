package com.booksearch.data.searchlog

import com.booksearch.data.fakes.FakeTimeProvider
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.SearchOutcome
import com.booksearch.domain.model.SearchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SearchLogTest {

    private val time = FakeTimeProvider(nowMillis = 1_000)
    private val log = SearchLog(time)

    private fun result(query: String, books: Int = 0, fromCache: Boolean = false) =
        SearchResult(query, List(books) { Book("OL${it}W", "t", emptyList(), null, 0, null) }, fromCache)

    @Test
    fun `given a search that succeeds, when tracked, then it is logged with its books, origin and duration`() = runTest {
        // When
        val returned = log.track("tolkien") {
            time.advanceBy(250)
            result("tolkien", books = 3)
        }

        // Then
        assertEquals(result("tolkien", books = 3), returned)
        val entry = log.entries.value.single()
        assertEquals("tolkien", entry.query)
        assertEquals(1_000, entry.startedAtMillis)
        assertEquals(SearchOutcome.Success(bookCount = 3, fromCache = false, durationMillis = 250), entry.outcome)
    }

    @Test
    fun `given a search that fails, when tracked, then it is logged as failed and the error is rethrown`() = runTest {
        // Given
        val failure = DomainException(DomainError.Server(404))

        // When
        try {
            log.track("getfail001") {
                time.advanceBy(40)
                throw failure
            }
            fail("Expected DomainException")
        } catch (e: DomainException) {
            assertSame(failure, e)
        }

        // Then
        assertEquals(SearchOutcome.Failed(DomainError.Server(404), durationMillis = 40), log.entries.value.single().outcome)
    }

    @Test
    fun `given a search that is cancelled, when tracked, then it is logged with the time until cancellation and still cancels`() = runTest {
        // When
        try {
            log.track("dun") {
                time.advanceBy(412)
                throw CancellationException("new search")
            }
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("new search", e.message)
        }

        // Then
        assertEquals(SearchOutcome.Cancelled(afterMillis = 412), log.entries.value.single().outcome)
    }

    @Test
    fun `given more searches than the limit, when tracked, then only the newest are kept newest first`() = runTest {
        // When
        repeat(SearchLog.MAX_ENTRIES + 5) { i -> log.track("q$i") { result("q$i") } }

        // Then
        val queries = log.entries.value.map { it.query }
        assertEquals(SearchLog.MAX_ENTRIES, queries.size)
        assertEquals("q${SearchLog.MAX_ENTRIES + 4}", queries.first())
        assertEquals("q5", queries.last())
        assertEquals(queries.size, log.entries.value.map { it.id }.toSet().size)
    }

    @Test
    fun `given logged searches, when cleared, then the log is empty`() = runTest {
        // Given
        log.track("a") { result("a") }

        // When
        log.clear()

        // Then
        assertTrue(log.entries.value.isEmpty())
    }
}
