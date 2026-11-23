package com.booksearch.app.testing

import com.booksearch.domain.model.CachedSearch
import com.booksearch.domain.repository.CacheStatusRepository

class FakeCacheStatusRepository(var searches: List<CachedSearch> = emptyList()) : CacheStatusRepository {
    override suspend fun cachedSearches(): List<CachedSearch> = searches
}
