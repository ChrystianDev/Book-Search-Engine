package com.booksearch.domain.model

/**
 * @property fromCache true when the books were served from the in-memory cache
 * without hitting the network.
 */
data class SearchResult(
    val query: String,
    val books: List<Book>,
    val fromCache: Boolean,
)
