package com.booksearch.domain.repository

import com.booksearch.domain.model.CachedSearch

/** Read-only view of the active cache backend, for diagnostics (Settings → Caché → Info estado). */
interface CacheStatusRepository {

    /** Searches stored in the active cache that returned at least one book, expired ones included. */
    suspend fun cachedSearches(): List<CachedSearch>
}
