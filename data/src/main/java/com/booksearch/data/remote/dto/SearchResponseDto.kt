package com.booksearch.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val docs: List<BookDocDto> = emptyList(),
)

/** Trending uses the same document shape as search when requested with the same `fields`. */
@Serializable
data class TrendingResponseDto(
    val works: List<BookDocDto> = emptyList(),
)

@Serializable
data class BookDocDto(
    val key: String? = null,
    val title: String? = null,
    @SerialName("author_name") val authorNames: List<String> = emptyList(),
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("edition_count") val editionCount: Int = 0,
)
