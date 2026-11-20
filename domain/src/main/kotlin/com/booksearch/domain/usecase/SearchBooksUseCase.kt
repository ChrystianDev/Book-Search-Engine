package com.booksearch.domain.usecase

import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.BookRepository
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    /**
     * Searches books for [query] after trimming it.
     * A query shorter than [MIN_QUERY_LENGTH] (blank included) never reaches the repository
     * and yields an empty result.
     */
    suspend operator fun invoke(query: String): SearchResult {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            return SearchResult(query = normalizedQuery, books = emptyList(), fromCache = false)
        }
        return repository.searchBooks(normalizedQuery)
    }

    companion object {
        /**
         * Open Library rejects shorter searches with HTTP 422 ("Query too short, must be at
         * least 3 characters"), so they are not sent at all.
         */
        const val MIN_QUERY_LENGTH = 3
    }
}
