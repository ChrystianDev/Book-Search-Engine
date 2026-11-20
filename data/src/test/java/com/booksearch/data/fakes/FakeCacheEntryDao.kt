package com.booksearch.data.fakes

import com.booksearch.data.cache.room.CacheEntryDao
import com.booksearch.data.cache.room.CacheEntryEntity

/** In-memory stand-in for the Room DAO: same semantics, no Android runtime needed. */
class FakeCacheEntryDao : CacheEntryDao {

    val entries = mutableMapOf<Pair<String, String>, CacheEntryEntity>()

    /** When set, every operation throws it, simulating a storage failure. */
    var failure: Throwable? = null

    private fun failIfNeeded() {
        failure?.let { throw it }
    }

    override suspend fun get(scope: String, key: String): CacheEntryEntity? = run {
        failIfNeeded()
        entries[scope to key]
    }

    override suspend fun getAll(scope: String): List<CacheEntryEntity> = run {
        failIfNeeded()
        entries.values.filter { it.scope == scope }
    }

    override suspend fun upsert(entry: CacheEntryEntity) {
        failIfNeeded()
        entries[entry.scope to entry.cacheKey] = entry
    }

    override suspend fun delete(scope: String, key: String) {
        failIfNeeded()
        entries.remove(scope to key)
    }

    override suspend fun count(scope: String): Int = run {
        failIfNeeded()
        entries.count { it.key.first == scope }
    }

    override suspend fun deleteOldest(scope: String, count: Int) {
        failIfNeeded()
        entries.filter { it.key.first == scope }
            .entries
            .sortedBy { it.value.storedAtMillis }
            .take(count)
            .forEach { entries.remove(it.key) }
    }

    override suspend fun clearAll() = entries.clear()
}
