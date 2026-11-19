package com.booksearch.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The Subjects API uses a different shape than search: `cover_id` and `authors[{key, name}]`. */
@Serializable
data class SubjectResponseDto(
    val works: List<SubjectWorkDto> = emptyList(),
)

@Serializable
data class SubjectWorkDto(
    val key: String? = null,
    val title: String? = null,
    val authors: List<SubjectAuthorDto> = emptyList(),
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("cover_id") val coverId: Long? = null,
    @SerialName("edition_count") val editionCount: Int = 0,
)

@Serializable
data class SubjectAuthorDto(
    val name: String? = null,
)
