package com.booksearch.domain.model

/** Cached as JSON by the disk cache (Room). */
@kotlinx.serialization.Serializable
data class BookDetail(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val description: String?,
    val subjects: List<String>,
    val coverUrl: String?,
)
