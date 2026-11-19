package com.booksearch.domain.usecase

import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.BookRepository
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    /**
     * Searches books for [query] after trimming it.
     * A blank query never reaches the repository and yields an empty result.
     */
    suspend operator fun invoke(query: String): SearchResult {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return SearchResult(query = normalizedQuery, books = emptyList(), fromCache = false)
        }
        return repository.searchBooks(normalizedQuery)
    }
}
