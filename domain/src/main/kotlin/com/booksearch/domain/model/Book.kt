package com.booksearch.domain.model

/**
 * A book as shown in lists (search results, trending carousel, subject rows).
 *
 * Optional API fields are already resolved here: lists are never null and
 * missing values are explicit nullable types.
 *
 * @kotlinx.serialization.Serializable: the disk cache (Room) stores books as JSON. Serialization is a pure-Kotlin
 * concern here, so the domain module keeps no Android dependencies.
 */
@kotlinx.serialization.Serializable
data class Book(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val editionCount: Int,
    val coverUrl: String?,
)
