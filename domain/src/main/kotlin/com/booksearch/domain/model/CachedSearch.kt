package com.booksearch.domain.model

/**
 * A search whose results are stored in the active cache.
 *
 * [query] is the normalized cache key (trimmed, lowercase). [expiresAtMillis] is when the
 * entry stops being served; expired entries may still be listed until the cache removes
 * them on the next read of that query.
 */
data class CachedSearch(
    val query: String,
    val expiresAtMillis: Long,
)
