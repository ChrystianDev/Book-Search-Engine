package com.booksearch.domain.model

/**
 * A book as shown in lists (search results, trending carousel, subject rows).
 *
 * Optional API fields are already resolved here: lists are never null and
 * missing values are explicit nullable types.
 */
data class Book(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val coverUrl: String?,
)
