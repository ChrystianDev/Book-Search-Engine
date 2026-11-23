package com.booksearch.data.remote.dto

import com.booksearch.data.remote.serializer.TextValueSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkDto(
    val key: String? = null,
    val title: String? = null,
    @Serializable(with = TextValueSerializer::class)
    val description: String? = null,
    val authors: List<WorkAuthorDto> = emptyList(),
    val covers: List<Long> = emptyList(),
    @SerialName("first_publish_date") val firstPublishDate: String? = null,
    val subjects: List<String> = emptyList(),
)

/** Authors in a work are references: `{ "author": { "key": "/authors/OL26320A" } }`. */
@Serializable
data class WorkAuthorDto(
    val author: KeyRefDto? = null,
)

@Serializable
data class KeyRefDto(
    val key: String,
)

@Serializable
data class AuthorDto(
    val name: String? = null,
    @SerialName("personal_name") val personalName: String? = null,
)
