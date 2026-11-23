package com.booksearch.data.repository

import com.booksearch.data.cache.BookCacheStore
import com.booksearch.domain.model.CachedSearch
import com.booksearch.domain.repository.CacheStatusRepository
import javax.inject.Inject

/** Lists the search cache of the active backend (memory or disk), skipping searches without results. */
class CacheStatusRepositoryImpl @Inject constructor(
    private val caches: BookCacheStore,
) : CacheStatusRepository {

    override suspend fun cachedSearches(): List<CachedSearch> =
        caches.current().search.snapshot()
            .filter { it.value.isNotEmpty() }
            .map { CachedSearch(query = it.key, expiresAtMillis = it.expiresAtMillis) }
}
