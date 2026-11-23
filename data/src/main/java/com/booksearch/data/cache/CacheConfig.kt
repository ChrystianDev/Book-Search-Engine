package com.booksearch.data.cache

data class CacheConfig(
    val ttlMillis: Long = InMemoryCache.DEFAULT_TTL_MILLIS,
    val maxEntries: Int = InMemoryCache.DEFAULT_MAX_ENTRIES,
)
