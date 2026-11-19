package com.booksearch.domain.repository

import com.booksearch.domain.error.DomainException
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.model.SearchResult

/**
 * All functions are main-safe and cancellable. Failures are reported as [DomainException].
 */
interface BookRepository {
    /** [query] is expected to be already trimmed and non-blank. */
    suspend fun searchBooks(query: String): SearchResult

    suspend fun getTrendingBooks(): List<Book>

    suspend fun getBooksBySubject(subject: BookSubject): List<Book>

    suspend fun getBookDetail(workId: String): BookDetail
}
