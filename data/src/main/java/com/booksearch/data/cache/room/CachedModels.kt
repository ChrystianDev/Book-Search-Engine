package com.booksearch.data.cache.room

import kotlinx.serialization.Serializable

/*
 * Storage format of the disk cache: the JSON written to `cache_entries.json`.
 *
 * Separate from the domain models on purpose (rule 4): renaming a domain field must not
 * silently change what is stored on disk. Changing a field here changes the stored format:
 * old entries that no longer decode are treated as cache misses.
 *
 * Field names match the format written by earlier versions (which serialized the domain
 * models directly), so entries already on disk keep decoding.
 */

@Serializable
internal data class CachedBook(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val editionCount: Int,
    val coverUrl: String?,
)

@Serializable
internal data class CachedBookDetail(
    val workId: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val description: String?,
    val subjects: List<String>,
    val coverUrl: String?,
)
