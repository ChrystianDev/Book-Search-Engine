package com.booksearch.domain.model

data class BookDetail(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val description: String?,
    val subjects: List<String>,
    val coverUrl: String?,
)
