package com.booksearch.app.testing

import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.BookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Fake repository with simulated network latency ([searchLatencyMillis]) driven by virtual time.
 * It records which searches were requested, completed or cancelled.
 */
class FakeBookRepository : BookRepository {

    var searchLatencyMillis = 1_000L
    var searchResponse: (query: String) -> SearchResult = { query ->
        SearchResult(query, listOf(aBook("$query-1")), fromCache = false)
    }

    val requestedSearches = mutableListOf<String>()
    val completedSearches = mutableListOf<String>()
    val cancelledSearches = mutableListOf<String>()

    override suspend fun searchBooks(query: String): SearchResult {
        requestedSearches += query
        try {
            delay(searchLatencyMillis)
        } catch (e: CancellationException) {
            cancelledSearches += query
            throw e // always rethrow: cancellation must propagate
        }
        return searchResponse(query).also { completedSearches += query }
    }

    override suspend fun getTrendingBooks(): List<Book> = listOf(aBook("trending"))

    override suspend fun getBooksBySubject(subject: BookSubject): List<Book> = listOf(aBook(subject.name))

    var detailLatencyMillis = 0L
    var detailResponse: (workId: String) -> BookDetail = { aBookDetail(it) }

    override suspend fun getBookDetail(workId: String): BookDetail {
        delay(detailLatencyMillis)
        return detailResponse(workId)
    }
}

fun aBook(id: String) = Book(
    workId = id,
    title = "Title $id",
    authors = listOf("Author"),
    firstPublishYear = 2000,
    editionCount = 1,
    coverUrl = null,
)

fun aBookDetail(id: String) = BookDetail(
    workId = id,
    title = "Title $id",
    authors = listOf("Author"),
    firstPublishYear = 2000,
    description = "Description",
    subjects = listOf("Subject"),
    coverUrl = null,
)
